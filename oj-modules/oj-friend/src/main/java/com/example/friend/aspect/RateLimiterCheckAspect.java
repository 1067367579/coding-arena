package com.example.friend.aspect;

import com.example.common.core.constants.JwtConstants;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.annotation.CheckRateLimiter;
import com.example.friend.manager.RedisLimiterManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RateLimiterCheckAspect {

    @Autowired
    private RedisLimiterManager redisLimiterManager;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, CheckRateLimiter rateLimit) throws Throwable {
        String userId = ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID).toString();
        redisLimiterManager.doRateLimit(userId,
                rateLimit.rate(),
                rateLimit.interval(),
                RateType.OVERALL);
        return joinPoint.proceed();
    }
}
