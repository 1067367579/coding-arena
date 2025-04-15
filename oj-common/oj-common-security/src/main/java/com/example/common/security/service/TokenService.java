package com.example.common.security.service;

import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.core.constants.JwtConstants;
import com.example.core.domain.LoginUser;
import com.example.core.utils.JWTUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private RedisService redisService;

    /**
     *  创建令牌 userId为key value保存用户的身份
     */
    public String createToken(Long userId,String nickName,
                              String avatar,
                              Integer identity,String secret) {
        //验证通过 签发令牌 JWT只存储唯一标识信息 并不能确定用户的身份 比如管理员
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstants.USER_ID,userId);
        refreshToken(userId, identity,nickName,avatar);
        return JWTUtils.createToken(map, secret);
    }

    /**
     * 令牌详细信息放到redis中 控制有效时间
     */
    private void refreshToken(Long userId, Integer identity,String nickName,String avatar) {
        /*
            第三方组件存储敏感信息 redis表明用户身份字段
            身份认证具体要存储哪些信息 identity 1 表示普通用户 2 表示管理员用户 对象 考虑扩展性
            使用什么样的数据结构 String hash list zset set 此处不需要集合 简单存储对象 使用高效的String
            key必须保证唯一 便于维护 统一前缀 loginToken:userId 唯一的 userId是雪花算法生成
            过期时间如何记录 过期时间应该定多长 根据用户表现 720分钟
         */
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        loginUser.setAvatar(avatar);
        redisService.setCacheObject(getRedisKey(userId),
                loginUser, CacheConstants.LOGIN_TTL, TimeUnit.MINUTES);
    }

    /**
     * 续签令牌逻辑 这里返回错误不合适，因为用户身份校验已经通过 是后端自己的业务错误
     */
    public void extendToken(String token,String secret) {
        //先判断令牌是否合法 拿出令牌的redis key
        Claims claims;
        try {
             claims = JWTUtils.parseToken(token, secret);
             if(claims == null) {
                 //延长失败 直接return 后端业务逻辑错误 需要记录日志 此处延长失败不影响请求的处理
                 log.error("解析Token出现异常,解析出的claims为null");
                 return;
             }
        } catch (Exception ex) {
            //直接return
            log.error("解析Token出现异常，{}",ex.getMessage());
            return;
        }
        Long userId = claims.get(JwtConstants.USER_ID, Long.class);
        //到redis中获取有效时间
        Long ttl = redisService.getExpire(getRedisKey(userId),TimeUnit.MINUTES);
        //判断ttl是否小于三小时
        if(ttl <= CacheConstants.LOGIN_EXTEND_TTL) {
            //延长到十二个小时
            redisService.expire(getRedisKey(userId), CacheConstants.LOGIN_TTL
                    ,TimeUnit.MINUTES);
        }
    }

    private static String getRedisKey(Long userId) {
        return CacheConstants.USER_TOKEN_PREFIX + userId;
    }

    /**
     * 获取用户对象返回
     */
    public LoginUser getLoginUser(String token,String secret) {
        Long userId = getUserKey(token, secret);
        return redisService.getCacheObject(getRedisKey(userId),LoginUser.class);
    }

    /**
     * 从token中获取用户唯一标识
     */
    public Long getUserKey(String token, String secret) {
        Claims claims = JWTUtils.parseToken(token, secret);
        return claims.get(JwtConstants.USER_ID, Long.class);
    }

    /**
     * 从redis中删除用户对象
     */
    public boolean deleteLoginUser(String token,String secret) {
        Long userId = getUserKey(token, secret);
        return redisService.deleteObject(getRedisKey(userId));
    }
}
