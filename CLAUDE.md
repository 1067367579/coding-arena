# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Coding Arena is a microservices-based Online Judge system for algorithm competitions, programming practice, and code evaluation. Built with Spring Cloud Alibaba stack.

## Build Commands

```bash
# Build entire project
mvn clean install -DskipTests

# Build specific module
mvn clean install -DskipTests -pl oj-modules/oj-system
mvn clean install -DskipTests -pl oj-modules/oj-judge
mvn clean install -DskipTests -pl oj-modules/oj-friend
mvn clean install -DskipTests -pl oj-modules/oj-job
mvn clean install -DskipTests -pl oj-gateway

# Run tests
mvn test

# Run single test class
mvn test -Dtest=SysUserControllerTest
```

## Architecture

### Microservices

| Service | Port | Responsibility |
|---------|------|----------------|
| oj-gateway | 8080 | API gateway, JWT auth, rate limiting |
| oj-friend | - | C-end user service (questions, submissions, exams) |
| oj-judge | - | Code execution engine (Docker sandbox) |
| oj-system | - | Admin management (questions, contests, users) |
| oj-job | - | Scheduled tasks (XXL-Job) |

### Data Flow

User submission → Gateway (JWT verify) → oj-friend (rate limit) → RabbitMQ → oj-judge (Docker execute) → Redis/MySQL

### Key Technologies

- Spring Boot 3.0.1, Java 17
- Spring Cloud 2022.0.0 + Spring Cloud Alibaba 2022.0.0.0-RC2
- MySQL 8.0, Redis 7.0, RabbitMQ 3.11, Elasticsearch 8.5
- Docker Java SDK 3.3.4 (for code sandbox)
- Redisson 3.24 (distributed rate limiter)
- XXL-Job 2.4.0 (scheduled tasks)

### Common Patterns

- **JWT Auth**: Gateway validates JWT, Redis stores tokens (`login:token:{userId}`)
- **Rate Limiting**: Redisson RRateLimiter via `@CheckRateLimiter` annotation
- **Async Processing**: RabbitMQ queues for code submission
- **Caching**: Redis with String, List, ZSet for different data types
- **Docker Sandbox**: ArrayBlockingQueue pool for container reuse (5 containers)

### Important Constants

Cache keys defined in `oj-common/oj-common-core/src/main/java/com/example/common/core/constants/CacheConstants.java`:
- `USER_TOKEN_PREFIX = "login:token:"`
- `QUESTION_LIST_KEY = "question:list"`
- `EXAM_RANK_LIST_KEY_PREFIX = "exam:rank:list:"`
- `HOT_QUESTION_LIST_KEY = "hot:question:list"`
- `SUBMIT_LIMITER_KEY_PREFIX = "submit:limiter:"`

### Core Files

- Gateway AuthFilter: `oj-gateway/src/main/java/com/example/gateway/filter/AuthFilter.java`
- Docker Pool: `oj-modules/oj-judge/src/main/java/com/example/judge/config/DockerSandBoxPool.java`
- Rate Limiter: `oj-modules/oj-friend/src/main/java/com/example/friend/manager/RedisLimiterManager.java`
- Judge Service: `oj-modules/oj-judge/src/main/java/com/example/judge/service/impl/JudgeServiceImpl.java`

### Configuration

All services use Nacos for config management. Bootstrap.yml contains Nacos connection:
```yaml
spring.cloud.nacos.config.server-addr: http://121.37.19.15:8848
spring.cloud.nacos.discovery.server-addr: http://121.37.19.15:8848
```
