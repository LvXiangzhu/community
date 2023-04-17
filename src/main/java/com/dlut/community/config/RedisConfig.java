package com.dlut.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) { //定义Bean时方法上声明参数，Spring容器会自动注入RedisConnectionFactory Bean
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //template需要用工厂连接才能具备访问数据库的能力
        template.setConnectionFactory(factory);

        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        // 使设置生效
        template.afterPropertiesSet();
        return template;
    }
}
