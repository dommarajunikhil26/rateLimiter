package com.nikhil.rateLimiter.service;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public SlidingWindowRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkRateLimiting(String key, int limit, int windowSeconds) {
        long now = System.currentTimeMillis();
        long range = now - (windowSeconds * 1000L);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, range);
        Long requestsRemaining = redisTemplate.opsForZSet().zCard(key);
        if(requestsRemaining != null && requestsRemaining >= limit){
            return false;
        }
        redisTemplate.opsForZSet().add(key, String.valueOf(now) + ":" + UUID.randomUUID(), now);
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        return true;
    }
}
