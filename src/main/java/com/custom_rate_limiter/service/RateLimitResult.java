package com.custom_rate_limiter.service;

import org.springframework.stereotype.Service;


public record RateLimitResult(boolean allowed, long remainig) {
}
