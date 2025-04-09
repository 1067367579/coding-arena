package com.example.common.redis.config;

import com.example.common.redis.JsonRedisSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig {
    /**
     * 初始化RedisTemplate对象
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory factory) {
        //先new对象
        RedisTemplate<Object,Object> redisTemplate = new RedisTemplate<>();
        //设置属性 连接工厂
        redisTemplate.setConnectionFactory(factory);
        //创建自定义序列化器
        JsonRedisSerializer redisSerializer = new JsonRedisSerializer(Object.class);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //Hash的key使用StringRedisSerializer序列化方式
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //key和value的序列化器不一样 key都是string类型 直接用字符串序列化器即可
        redisTemplate.setValueSerializer(redisSerializer);
        //value需要对对象进行序列化
        redisTemplate.setHashValueSerializer(redisSerializer);
        //完成初始化的一些操作
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
