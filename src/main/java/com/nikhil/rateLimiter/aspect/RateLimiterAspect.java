package com.nikhil.rateLimiter.aspect;

import com.nikhil.rateLimiter.annotation.RateLimit;
import com.nikhil.rateLimiter.exception.RateLimitExceededException;
import com.nikhil.rateLimiter.service.SlidingWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.invoke.MethodHandles;

@Aspect
public class RateLimiterAspect {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;

    public RateLimiterAspect(SlidingWindowRateLimiter slidingWindowRateLimiter) {
        this.slidingWindowRateLimiter = slidingWindowRateLimiter;
    }

    @Before("@annotation(rateLimit)")
    public void rateLimiter(JoinPoint joinPoint, RateLimit rateLimit) {
        int limit = rateLimit.limit();
        int windowSeconds = rateLimit.windowSeconds();

        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.isEmpty()){
            ipAddress = request.getRemoteAddr();
        }
        String methodName = joinPoint.getSignature().toShortString();
        String key = "rate_limit:" + ipAddress + ":" + methodName;

        boolean allowed = slidingWindowRateLimiter.checkRateLimiting(key, limit, windowSeconds);
        if(!allowed) {
            throw new RateLimitExceededException("Too many requests");
        }
    }
}
