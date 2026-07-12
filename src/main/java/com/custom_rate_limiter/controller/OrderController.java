package com.custom_rate_limiter.controller;


import com.custom_rate_limiter.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @RateLimit(capacity = 5, refillRate = 1)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("message", "Order created", "data", req));
    }

    @RateLimit(capacity = 50, refillRate = 10)
    @GetMapping
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "item", "Laptop"),
                Map.of("id", 2, "item", "Mouse")
        ));
    }
}