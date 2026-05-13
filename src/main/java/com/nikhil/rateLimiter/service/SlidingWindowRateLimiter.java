package com.nikhil.rateLimiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public SlidingWindowRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimiting(String ipAddress, int limit, int windowSeconds) {
        //
    }
}
