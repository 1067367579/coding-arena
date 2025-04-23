package com.example.friend.rabbit;

import com.example.common.core.constants.RabbitMQConstants;
import com.example.friend.manager.ExamRankCacheManager;
import com.example.friend.manager.MessageCacheManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheRefreshConsumer {

    @Autowired
    private MessageCacheManager messageCacheManager;

    @Autowired
    private ExamRankCacheManager examRankCacheManager;

    @RabbitListener(queues=RabbitMQConstants.MESSAGE_CACHE_REFRESH_QUEUE)
    public void consumeMessageRefresh(Long userId) {
        messageCacheManager.refreshCache(userId);
    }

    @RabbitListener(queues=RabbitMQConstants.EXAM_RANK_CACHE_REFRESH_QUEUE)
    public void consumeExamRankRefresh(Long examId) {
        examRankCacheManager.refreshCache(examId);
    }
}
