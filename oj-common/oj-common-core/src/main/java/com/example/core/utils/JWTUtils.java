package com.example.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

/**
 * JWT工具类
 */
public class JWTUtils {

    /**
     * 生成令牌
     * @param claims 载荷
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String,Object> claims,String secret) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512,secret)
                .compact();
    }

    /**
     * 解析令牌
     * @param token 令牌
     * @param secret 密钥
     * @return 载荷数据
     */
    public static Claims parseToken(String token,String secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

}
