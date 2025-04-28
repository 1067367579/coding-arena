package com.example.system.manager;

import com.example.common.core.constants.CacheConstants;
import com.example.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component //维护题目列表的缓存
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;

    //如果插入了新的数据 就要刷新缓存 那就说明是新的数据 创建时间是最大的 头插即可解决
    public void addCache(Long questionId) {
        redisService.leftPushForList(CacheConstants.QUESTION_LIST_KEY, questionId);
    }

    //如果删除了数据 也要刷新缓存 直接按照数据从列表中删除
    public void removeCache(Long questionId) {
        redisService.removeForList(CacheConstants.QUESTION_LIST_KEY, questionId);
    }
}
