# Repository Guidelines

## Project Structure & Module Organization

This is a Java 17, Maven multi-module Spring Cloud project.

- `oj-gateway/`: Spring Cloud Gateway (routing, auth, rate limiting).
- `oj-modules/`: Business microservices:
  - `oj-modules/oj-friend/` (user-facing service)
  - `oj-modules/oj-judge/` (judge engine / sandbox integration)
  - `oj-modules/oj-job/` (scheduled jobs via XXL-Job)
  - `oj-modules/oj-system/` (admin/system service)
- `oj-common/`: Shared libraries (core, security, redis, mybatis, swagger, etc.).
- `oj-api/`: API/contracts module.
- `deploy/`: Deployment/support files (e.g., nginx config/scripts).
- Generated/runtime dirs (do not commit): `logs/`, `user-code/`, `user-code-pool/` (already in `.gitignore`).

## Build, Test, and Development Commands

Run from the repo root:

- `mvn -T 1C -DskipTests package`: Build all modules (fast local build).
- `mvn test`: Run unit/integration tests across modules.
- `mvn -pl oj-gateway spring-boot:run`: Run the gateway locally.
- `mvn -pl oj-modules/oj-judge spring-boot:run`: Run a specific service by module path.

Tip: many services rely on middleware (Nacos/Redis/MySQL/RabbitMQ). See `README.md` for the expected local stack and configuration.

## Coding Style & Naming Conventions

- Language: Java (Spring Boot 3.x / Spring Cloud).
- Indentation: 4 spaces; UTF-8 source encoding.
- Naming:
  - Packages: `com.example.<module>...`
  - Classes: `UpperCamelCase`; methods/fields: `lowerCamelCase`; constants: `UPPER_SNAKE_CASE`.
- Prefer Lombok patterns already used in the codebase; keep changes consistent with existing modules.

## Testing Guidelines

- Framework: JUnit 5 + `spring-boot-starter-test` (see `oj-modules/oj-system/pom.xml`).
- Location: `src/test/java/...`
- Naming: `*Test.java` (e.g., `SysUserControllerTest.java`).
- Run focused tests: `mvn -pl oj-modules/oj-system -Dtest=SysUserControllerTest test`.

## Commit & Pull Request Guidelines

- Commit messages in history are short, descriptive summaries (often Chinese), e.g. “完善Readme”, “修复…bug”, “完成…功能”.
- Use a similar style: one line, present tense, state intent; optionally prefix with module, e.g. `oj-judge: 修复沙箱超时`.
- PRs should include: clear description, linked issue/ticket (if any), service/module impact, and screenshots/log snippets for API behavior changes.

## Security & Configuration Tips

- Avoid committing secrets; keep environment-specific config in Nacos or local override files.
- Treat `user-code/` as untrusted input; never execute code outside the sandbox pathway.
