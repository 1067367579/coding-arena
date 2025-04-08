package com.example.common.security.service;

import com.example.common.redis.service.RedisService;
import com.example.common.security.entity.LoginUser;
import com.example.common.security.utils.JWTUtils;
import com.example.core.constants.JwtConstants;
import com.example.core.constants.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisService redisService;

    public String createToken(Long userId,Integer identity,String secret) {
        //验证通过 签发令牌 JWT只存储唯一标识信息 并不能确定用户的身份 比如管理员
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstants.USER_ID,userId);
        refreshToken(userId, identity);
        return JWTUtils.createToken(map, secret);
    }

    private void refreshToken(Long userId, Integer identity) {
        /*
            第三方组件存储敏感信息 redis表明用户身份字段
            身份认证具体要存储哪些信息 identity 1 表示普通用户 2 表示管理员用户 对象 考虑扩展性
            使用什么样的数据结构 String hash list zset set 此处不需要集合 简单存储对象 使用高效的String
            key必须保证唯一 便于维护 统一前缀 loginToken:userId 唯一的 userId是雪花算法生成
            过期时间如何记录 过期时间应该定多长 根据用户表现 720分钟
         */
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        redisService.setCacheObject(RedisConstants.USER_LOGIN_PREFIX+userId,
                loginUser,RedisConstants.LOGIN_TTL, TimeUnit.MINUTES);
    }
}
