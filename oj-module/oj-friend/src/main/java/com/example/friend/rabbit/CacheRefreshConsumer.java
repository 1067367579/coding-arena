package com.example.friend.rabbit;

import com.example.common.core.constants.RabbitMQConstants;
import com.example.friend.manager.MessageCacheManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheRefreshConsumer {

    @Autowired
    private MessageCacheManager messageCacheManager;

    @RabbitListener(queues=RabbitMQConstants.MESSAGE_CACHE_REFRESH_QUEUE)
    public void receive(Long userId) {
        messageCacheManager.refreshCache(userId);
    }
}
