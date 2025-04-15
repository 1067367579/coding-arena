package com.example.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.core.constants.HttpConstants;
import com.example.core.constants.JwtConstants;
import com.example.core.domain.LoginUser;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.core.utils.JWTUtils;
import com.example.gateway.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

//过滤器也是一个组件 要交给Spring管理
@Component
@RefreshScope
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private IgnoreWhiteProperties ignoreWhiteProperties;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取路径 进行白名单校验 若在白名单直接放行
        String url = request.getURI().getPath();
        if(matches(url,ignoreWhiteProperties.getWhiteList())) {
            return chain.filter(exchange);
        }
        //不在白名单 token校验 先拿出token
        String token = getToken(request);
        if(!StringUtils.hasLength(token)) {
            //若token获取失败 返回未授权错误
            return unauthorizedResponse(exchange,"令牌不能为空");
        }
        //载荷
        Claims claims;
        try {
            claims = JWTUtils.parseToken(token,secret);
            if(claims == null) {
                return unauthorizedResponse(exchange,"令牌已过期或验证错误");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange,"令牌已过期或验证错误");
        }
        //去redis中校验是否过期
        Long userId = claims.get(JwtConstants.USER_ID,Long.class);
        String redisKey = CacheConstants.USER_TOKEN_PREFIX +userId;
        boolean isLogin = redisService.hasKey(redisKey);
        if(!isLogin) {
            //查找不到redis中的key
            return unauthorizedResponse(exchange,"令牌已过期");
        }
        LoginUser loginUser = redisService.getCacheObject(redisKey, LoginUser.class);
        //查找到用户详细信息 进行身份与路径匹配校验 只有匹配才通过
        if(url.contains(HttpConstants.SYSTEM_URL_PREFIX) &&
            !CacheConstants.LOGIN_IDENTITY_ADMIN.equals(loginUser.getIdentity())) {
            return unauthorizedResponse(exchange,"令牌验证失败");
        }
        if(url.contains(HttpConstants.FRIEND_URL_PREFIX) &&
                !CacheConstants.LOGIN_IDENTITY_USER.equals(loginUser.getIdentity())) {
            return unauthorizedResponse(exchange,"令牌验证失败");
        }
        return chain.filter(exchange);
    }

    /**
     * 鉴权失败处理方法
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange,String msg) {
        log.error("鉴权失败,路径:{}",exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(),msg,
                ResultCode.FAILED_UNAUTHORIZED.getCode());
    }

    /**
     *  webFlux写入响应，Gateway不属于SpringMVC框架，不能使用全局异常处理
     */
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response,String msg,int code) {
        response.setStatusCode(HttpStatus.OK);
        //返回类型为Application/Json类型 也需要自己手动指定
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //构造返回对象
        Result<Void> result = Result.fail(code,msg);
        DataBuffer buffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(buffer));
    }


    /**
     * 从请求对象中获取token
     * @param request 请求对象
     * @return token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
        if(StringUtils.hasLength(token) && token.startsWith(HttpConstants.AUTHENTICATION_PREFIX)) {
            //获取成功 去掉前缀
            token = token.replaceFirst(HttpConstants.AUTHENTICATION_PREFIX, "");
        }
        return token;
    }

    /**
     * 遍历模式列表看url是否匹配
     * @param patternList 模式列表
     */
    public boolean matches(String url, List<String> patternList) {
        if(!StringUtils.hasLength(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        for (String pattern : patternList) {
            if(isMatch(url,pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断路径是否模式匹配
     * @param url 路径
     * @param pattern 模式
     */
    public boolean isMatch(String url, String pattern) {
        //使用AntPathMatcher类进行模式匹配 匹配规则中 ？为单个占位符 *表示一层路径的字符串 **表示多层
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    @Override
    public int getOrder() {
        return -200; //数值越小 越先被采用
    }
}
