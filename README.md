# TokenBucketRateLimiter

A Spring Boot 3.5.16 application that demonstrates a custom token bucket rate limiter backed by Redis and implemented with a Lua script.

## What it does

This project provides two rate-limiting approaches:

- **Annotation-based rate limiting** for specific controller methods using `@RateLimit`
- **IP-based request limiting** applied to all routes through a Spring MVC interceptor

It uses Redis to store token bucket state and a Lua script to perform atomic token consumption and refill logic.

## Features

- Token bucket algorithm powered by Redis Lua scripting
- Method-level rate limiting with a custom annotation
- Global IP-based request throttling
- `429 Too Many Requests` responses when limits are exceeded
- Remaining-token information returned in response headers for IP-limited requests
- Spring Boot AOP integration

## How it works

### 1. Method-level rate limiting

The custom annotation `@RateLimit` can be placed on controller methods. An aspect intercepts the call, resolves the user identity from the `X-USER-ID` header or client IP address, and consumes tokens from a Redis-backed bucket keyed by user and endpoint.

Example from the repo:

- `POST /api/orders` → `@RateLimit(capacity = 5, refillRate = 1)`
- `GET /api/orders` → `@RateLimit(capacity = 50, refillRate = 10)`

### 2. IP-based limiting

A `HandlerInterceptor` runs for all requests and applies a fixed limit per client IP:

- Capacity: `100`
- Refill rate: `20 tokens/second`

If the limit is exceeded, the API responds with:

- HTTP status `429`
- JSON body: `{"error": "Too Many Requests"}`

## Project structure

- `src/main/java/com/custom_rate_limiter/annotation/RateLimit.java` — custom rate-limit annotation
- `src/main/java/com/custom_rate_limiter/aspect/RateLimitAspect.java` — method-level enforcement
- `src/main/java/com/custom_rate_limiter/interceptor/IpRateLimitInterceptor.java` — IP-based enforcement
- `src/main/java/com/custom_rate_limiter/service/RateLimiterService.java` — Redis/Lua token consumption logic
- `src/main/java/com/custom_rate_limiter/config/RedisConfig.java` — Lua script configuration
- `src/main/java/com/custom_rate_limiter/config/WebConfig.java` — interceptor registration
- `src/main/java/com/custom_rate_limiter/controller/OrderController.java` — sample protected endpoints
- `src/main/resources/scripts/token_bucket.lua` — token bucket implementation

## Requirements

- Java 21
- Maven
- Redis

## Running the application

1. Start Redis locally.
2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn spring-boot:run
```

## Example usage

### Create an order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: user-123" \
  -d '{"item":"Keyboard"}'
```

### Get orders

```bash
curl http://localhost:8080/api/orders \
  -H "X-USER-ID: user-123"
```

## Notes

- If `X-USER-ID` is not provided, the system falls back to the client IP address.
- The Redis Lua script keeps token bucket updates atomic.
- This repository currently includes a basic sample controller (`OrderController`) to demonstrate the limiter behavior.

## License

No license file was present in the repository at the time this README was generated.
