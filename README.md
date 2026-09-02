# Distributed API Rate Limiter — Spring Boot, Redis & Lua

### Overview

Designed and implemented a **production-oriented API Rate Limiting system** using **Spring Boot 3.5, Redis, and Lua scripting** to protect APIs from excessive traffic, abuse, and resource exhaustion.

The system implements the **Token Bucket algorithm** and supports two complementary rate-limiting strategies:

* **Method-level rate limiting** using a custom `@RateLimit` annotation and Spring AOP.
* **Global IP-based rate limiting** using a Spring MVC `HandlerInterceptor`.

Redis acts as the distributed state store, while a **Lua script performs token refill and consumption atomically**, preventing race conditions when multiple requests access the same bucket concurrently.

---

## 🚀 Key Engineering Highlights

### 1. Redis-backed Token Bucket Algorithm

Implemented a token bucket where each client has:

* A configurable maximum bucket capacity.
* A configurable token refill rate.
* Tokens consumed for every accepted request.
* Automatic token regeneration based on elapsed time.

For example:

```text
POST /api/orders
Capacity   : 5 tokens
Refill     : 1 token/second

GET /api/orders
Capacity   : 50 tokens
Refill     : 10 tokens/second
```

This allows different APIs to have different traffic policies instead of applying a single global limit.

---

### 2. Atomic Rate Limiting with Redis Lua

The critical part of the implementation is the Redis Lua script.

A request can involve multiple operations:

```text
Read bucket state
      ↓
Calculate elapsed time
      ↓
Refill tokens
      ↓
Check availability
      ↓
Consume token
      ↓
Update Redis state
```

Performing these operations independently could introduce race conditions when multiple requests arrive simultaneously.

The project solves this by executing the complete token-bucket operation inside **one Redis Lua script**, making the state transition atomic.

Conceptually:

```text
Request
   ↓
Redis
   ↓
Lua Script
   ├── Read tokens + timestamp
   ├── Calculate refill
   ├── Check token availability
   ├── Consume token
   └── Store updated state
   ↓
Allowed / Rejected
```

This makes the implementation suitable for concurrent requests and horizontally scaled application instances sharing the same Redis state.

---

## ⚡ Method-Level Rate Limiting

Implemented a custom annotation:

```java
@RateLimit(capacity = 5, refillRate = 1)
```

The corresponding Spring AOP aspect intercepts annotated controller methods and determines the request identity using:

```text
X-USER-ID
     ↓
If unavailable
     ↓
Client IP
```

The bucket is then associated with the client and endpoint, allowing different APIs to maintain independent limits.

Example:

```text
user-123 + POST /api/orders
              ↓
Redis Bucket
              ↓
5 capacity / 1 refill per second
```

This demonstrates practical usage of **custom annotations, Spring AOP, request interception, and cross-cutting concerns**.

---

## 🌐 Global IP-Based Rate Limiting

Implemented a Spring MVC `HandlerInterceptor` to protect all application routes with a global IP-based limit.

Default policy:

```text
Bucket Capacity : 100
Refill Rate     : 20 tokens/second
```

The interceptor executes before the controller and prevents excessive requests from reaching the business layer.

When the bucket is exhausted:

```http
HTTP/1.1 429 Too Many Requests
```

Response:

```json
{
  "error": "Too Many Requests"
}
```

For accepted requests, the API also exposes **remaining-token information through response headers**, making the limiter observable to API clients.

---

## 🧠 Why This Project Matters

Traditional applications often allow every incoming request to reach the controller and database.

Under excessive traffic:

```text
Thousands of Requests
        ↓
Application Threads
        ↓
Business Logic
        ↓
Database / External APIs
        ↓
Resource Exhaustion
```

The rate limiter introduces a control layer:

```text
Incoming Request
        ↓
Rate Limiter
        ↓
 ┌──────┴──────┐
 ↓             ↓
Allowed       Rejected
 ↓             ↓
Controller     429
```

This protects backend resources and provides a foundation for building **resilient and scalable APIs**.

---

## 🏗️ Architecture

```text
                    Client
                      │
                      ▼
              Spring Boot API
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
   IP Rate Limiter          @RateLimit
   Interceptor                  AOP
          │                       │
          └───────────┬───────────┘
                      ▼
              RateLimiterService
                      │
                      ▼
                   Redis
                      │
                      ▼
                Lua Script
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
      Allowed                   Rejected
          │                       │
          ▼                       ▼
     Controller             HTTP 429
```

---

## 🛠️ Technology Stack

**Backend**

* Java 21
* Spring Boot 3.5
* Spring MVC
* Spring AOP

**Distributed State & Rate Limiting**

* Redis
* Redis Lua scripting
* Token Bucket algorithm

**Build & Infrastructure**

* Maven

---

## 📌 Core Concepts Demonstrated

This project demonstrates practical understanding of:

* API Rate Limiting
* Token Bucket Algorithm
* Redis
* Redis Lua Scripting
* Atomic operations
* Race-condition prevention
* Concurrent request handling
* Spring AOP
* Custom Java annotations
* Spring MVC Interceptors
* HTTP `429 Too Many Requests`
* Distributed application state
* API resource protection
* Configurable traffic policies

---

## 💡 Engineering Takeaway

The major learning from this project was that **rate limiting is not simply checking a counter**.

In a concurrent/distributed environment, the challenge is maintaining **correct shared state while multiple requests are modifying it simultaneously**.

Using Redis as the shared state store and Lua to perform the complete token-bucket operation atomically provides a much more reliable design than maintaining rate-limit state inside an individual application instance.

This project therefore focuses not only on implementing a feature, but on understanding **concurrency, distributed state, atomicity, and backend resilience**.
