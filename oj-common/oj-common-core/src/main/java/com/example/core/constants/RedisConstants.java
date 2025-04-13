package com.example.core.constants;

public class RedisConstants {
    public static final String USER_TOKEN_PREFIX = "login:token:";
    public static final Long LOGIN_TTL = 720L;
    public static final Integer LOGIN_IDENTITY_ADMIN = 2;
    public static final Integer LOGIN_IDENTITY_USER = 1;
    public static final Long LOGIN_EXTEND_TTL = 180L;
    public static final String EMAIL_CODE_PREFIX = "email:code:";
    public static final Long EMAIL_CODE_TTL = 120L;
    public static final String CODE_TIME_KEY = "code:time:";
}
