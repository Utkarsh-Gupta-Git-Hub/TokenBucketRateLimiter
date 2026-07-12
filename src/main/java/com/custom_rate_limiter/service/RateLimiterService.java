package com.custom_rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimiterService
{

private final StringRedisTemplate redisTemplate;
private final DefaultRedisScript<List> tokenBucketScript;

public RateLimitResult tryConsume(String key, int capacity , int refillRate,int tokenRequested){
    long now = System.currentTimeMillis();

    List<Long> result = redisTemplate.execute(tokenBucketScript, Collections.singletonList(key), String.valueOf(capacity), String.valueOf(refillRate), String.valueOf(now), String.valueOf(tokenRequested));
  boolean allowed = result.get(0) == 1L;
  long remainig = result.get(1);
  return new RateLimitResult(allowed,remainig);

}
}
