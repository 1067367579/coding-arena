package com.example.friend.annotation;

import org.redisson.api.RateIntervalUnit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRateLimiter {
    int rate() default 3; // 令牌数
    int interval() default 5; // 时间单位
    RateIntervalUnit unit() default RateIntervalUnit.SECONDS; // SECONDS/MINUTES
}
