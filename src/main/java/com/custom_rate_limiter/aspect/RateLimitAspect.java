package com.custom_rate_limiter.aspect;

import com.custom_rate_limiter.annotation.RateLimit;
import com.custom_rate_limiter.exception.RateLimitExceededException;
import com.custom_rate_limiter.service.RateLimitResult;
import com.custom_rate_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;
    @Around("@annotation(rateLimit)")
    public Object enforce(ProceedingJoinPoint  joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        String userId = resolveUserId(request);
        String endpoint = joinPoint.getSignature().toShortString();
        String key = "rate_limit:user:" + userId + ":endpoint:" + endpoint;
        log.info(">>> ASPECT TRIGGERED - key: " + key);
        RateLimitResult result = rateLimiterService.tryConsume(key, rateLimit.capacity(), rateLimit.refillRate(), 1);
        log.info(">>> allowed=" + result.allowed() + " remaining=" + result.remainig());
        if(!result.allowed()) {
            throw new RateLimitExceededException("Rate limit exceeded for this endpoint");
        }

return joinPoint.proceed();
    }

    private String resolveUserId(HttpServletRequest request) {
    String userId = request.getHeader("X-USER-ID");
    return (userId != null && !userId.isEmpty()) ? userId :request.getRemoteAddr();

    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }


}
