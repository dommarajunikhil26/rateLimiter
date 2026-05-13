package com.nikhil.rateLimiter.aspect;

import com.nikhil.rateLimiter.annotation.RateLimit;
import com.nikhil.rateLimiter.service.SlidingWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimiterAspect {
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;

    public RateLimiterAspect(SlidingWindowRateLimiter slidingWindowRateLimiter) {
        this.slidingWindowRateLimiter = slidingWindowRateLimiter;
    }

    @Before("@annotation(rateLimit)")
    public void rateLimiter(JoinPoint joinPoint, RateLimit rateLimit) {
        int limit = rateLimit.limit();
        int windowSeconds = rateLimit.windowSeconds();

        HttpServletRequest request = (HttpServletRequest) joinPoint.getThis();
        String ipAddress = request.getRemoteAddr();

        slidingWindowRateLimiter.checkRateLimiting(ipAddress, limit, windowSeconds);
    }
}
