package com.nikhil.rateLimiter.config;

import com.nikhil.rateLimiter.aspect.RateLimiterAspect;
import com.nikhil.rateLimiter.service.SlidingWindowRateLimiter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration(after = { RedisAutoConfiguration.class })
@ConditionalOnBean(RedisConnectionFactory.class)
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "rateLimiterRedisTemplate")
    public RedisTemplate<String, String> rateLimiterRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public SlidingWindowRateLimiter rateLimiter(RedisTemplate<String, String> rateLimiterRedisTemplate) {
        return new SlidingWindowRateLimiter(rateLimiterRedisTemplate);
    }

    @Bean
    public RateLimiterAspect rateLimiterAspect(SlidingWindowRateLimiter rateLimiter) {
        return new RateLimiterAspect(rateLimiter);
    }
}