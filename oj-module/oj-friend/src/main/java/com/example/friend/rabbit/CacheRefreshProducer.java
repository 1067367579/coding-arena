package com.example.friend.rabbit;

import com.example.common.core.constants.RabbitMQConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheRefreshProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void produceMessage(Long userId) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.MESSAGE_CACHE_REFRESH_QUEUE, userId);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAILED_RABBIT_PRODUCER);
        }
    }
}
