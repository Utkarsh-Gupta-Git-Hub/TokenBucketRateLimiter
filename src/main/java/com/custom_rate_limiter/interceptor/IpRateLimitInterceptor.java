package com.custom_rate_limiter.interceptor;

import com.custom_rate_limiter.service.RateLimitResult;
import com.custom_rate_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

@Service
@RequiredArgsConstructor
public class IpRateLimitInterceptor implements HandlerInterceptor {

private final RateLimiterService rateLimiterService;

private final int CAPACITY = 100;
private final int REFILL_RATE=20;

    /**
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

       String ip = extractClientIp(request);
       String key = "rate_limit:ip:"+ip;
        RateLimitResult result = rateLimiterService.tryConsume(key,CAPACITY,REFILL_RATE,1);

        response.setHeader("X-RateLimit-Remaining_IP", String.valueOf(result.remainig()));

        if(!result.allowed()){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\"}");
            return false;
        }
        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
