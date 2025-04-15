package com.example.core.constants;

public class CacheConstants {

    //当前登录用户信息存储
    public static final String USER_TOKEN_PREFIX = "login:token:";
    //令牌有效期
    public static final Long LOGIN_TTL = 720L;
    //管理员身份登录
    public static final Integer LOGIN_IDENTITY_ADMIN = 2;
    //普通用户身份登录
    public static final Integer LOGIN_IDENTITY_USER = 1;
    //延长的TTL
    public static final Long LOGIN_EXTEND_TTL = 180L;
    //邮箱验证码 key
    public static final String EMAIL_CODE_KEY_PREFIX = "email:code:";
    //计算当天获取验证码次数 key
    public static final String CODE_COUNTER_KEY_PREFIX = "code:counter:";

    //未完赛 key只有一份
    public static final String EXAM_UNFINISHED_LIST_KEY = "exam:unfinished:list";
    //历史竞赛 key只有一份
    public static final String EXAM_HISTORY_LIST_KEY = "exam:history:list";
    //竞赛详情 key有多份 取决于examId
    public static final String EXAM_DETAIL_KEY_PREFIX = "exam:detail:";
    public static final String USER_EXAM_LIST_KEY_PREFIX = "user:exam:list:";
}
