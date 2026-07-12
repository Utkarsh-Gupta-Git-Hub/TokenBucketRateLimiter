package com.custom_rate_limiter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RateLimitExceptionHandler {


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String,String>> handle (RateLimitExceededException  ex){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error",ex.getMessage()));
    }
}
