package com.example.common.security.intercptor;

import com.example.common.security.service.TokenService;
import com.example.common.core.constants.HttpConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

//这个拦截器是绑定在具体的某个微服务上的 并非网关 每个微服务都绑定这个拦截器 这个bean在引用的微服务中都有
//注意拦截器还需要在WebMvcConfiguration中挂载
@Component
@RefreshScope
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}") //从哪个地方拿 取决于这个bean是在哪个微服务管理
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = getToken(request);
        //网关已经校验过一次，如果没有令牌说明是semiLogin接口 直接放行
        if(!StringUtils.hasLength(token)) {
            return true;
        }
        //拿Claims 避免多次解析token
        Claims claims = tokenService.getClaims(token, secret);
        //已经在filter中经过了身份校验 获取token 然后进行续签操作
        tokenService.extendToken(claims);
        //获取出来用户ID的信息 然后设置ThreadLocal
        Long userId = tokenService.getUserKey(claims);
        ThreadLocalUtil.set(JwtConstants.USER_ID,userId);
        return true;
    }

    private static String getToken(HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpConstants.AUTHENTICATION);
            token = token.replaceFirst(HttpConstants.AUTHENTICATION_PREFIX, "");
            return token;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //此处是请求完成响应的时候执行的逻辑 数据传到前端之后的回调 此处要清理本次线程的副本
        ThreadLocalUtil.remove();
    }
}
