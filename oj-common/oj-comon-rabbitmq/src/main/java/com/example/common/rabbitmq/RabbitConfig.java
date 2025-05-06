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
        Map<String, Object> args = new HashMap<>();
        // 绑定死信交换机
        args.put("x-dead-letter-exchange", "dlx_exchange");
        // 指定死信路由键
        args.put("x-dead-letter-routing-key", "dlx.routing.key");
        // 设置队列最大消息数
        args.put("x-max-length", 5000);
        // 设置消息存活时间
        args.put("x-message-ttl", 600000); // 10分钟
        return new Queue(RabbitMQConstants.OJ_WORK_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlx_exchange");
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue("dlx.queue");
    }

    @Bean
    public Binding dlxBinding() {
        // 死信队列与交换机绑定（网页1、网页4）
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("dlx.routing.key");
    }
}
