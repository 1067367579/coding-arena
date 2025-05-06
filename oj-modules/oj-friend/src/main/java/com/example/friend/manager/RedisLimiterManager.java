package com.example.friend.manager;

import com.example.common.core.constants.CacheConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import jakarta.annotation.Resource;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 执行限流检查
     * @param key 用户唯一标识（如userId）
     * @param limit 时间窗口内允许的最大请求数
     * @param interval 时间窗口长度（秒）
     * @param rateType 限流作用域
     */
    public void doRateLimit(String key, int limit, int interval, RateType rateType) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(buildKey(key));

        // 原子化配置（避免重复初始化）
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(rateType, limit, interval, RateIntervalUnit.SECONDS);
            rateLimiter.expire(30L, TimeUnit.MINUTES);
        }

        if (!rateLimiter.tryAcquire(1)) {
            throw new ServiceException(ResultCode.FAILED_SUBMIT_FREQUENT);
        } else {
            //使用了一次令牌桶 动态续签
            rateLimiter.expire(30L, TimeUnit.MINUTES);
        }
    }

    private String buildKey(String identifier) {
        return CacheConstants.SUBMIT_LIMITER_KEY_PREFIX + identifier; // Key命名规范（网页2）
    }
}
