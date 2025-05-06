package com.example.common.core.constants;

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
    public static final String USER_DETAIL_KEY_PREFIX = "user:detail:";
    public static final String USER_UPLOAD_TIMES_KEY = "user:upload:time";
    public static final String QUESTION_LIST_KEY = "question:list";
    public static final String EXAM_QUESTION_LIST_KEY_PREFIX = "exam:question:list:";
    public static final String SUBMIT_KEY_PREFIX = "submit:";
    public static final String HOT_QUESTION_LIST_KEY = "hot:question:list";
    public static final String MESSAGE_DETAIL_KEY_PREFIX = "message:detail:";
    public static final String USER_MESSAGE_KEY_PREFIX = "user:message:";
    public static final String EXAM_RANK_LIST_KEY_PREFIX = "exam:rank:list:";
    public static final String SUBMIT_LIMITER_KEY_PREFIX = "submit:limiter:";
}
