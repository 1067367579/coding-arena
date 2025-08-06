package com.example.common.rabbitmq;

import com.example.common.core.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue messageCacheRefreshQueue() {
        return new Queue(RabbitMQConstants.MESSAGE_CACHE_REFRESH_QUEUE,true);
    }

    @Bean
    public Queue examCacheRefreshQueue() {
        return new Queue(RabbitMQConstants.EXAM_CACHE_REFRESH_QUEUE,true);
    }

    @Bean
    public Queue examRankCacheRefreshQueue() {
        return new Queue(RabbitMQConstants.EXAM_RANK_CACHE_REFRESH_QUEUE,true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue workQueue() {
        return new Queue(RabbitMQConstants.OJ_WORK_QUEUE, true);
    }
}
