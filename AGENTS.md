# Repository Guidelines

## Project Structure & Module Organization

This repository is a single-module Spring Boot 4.1.1 Web template using Java 21 and Maven. Production code lives in `src/main/java/org/example/`: `Application` is the minimal entry point, `controller/` contains HTTP endpoints, and `config/` contains infrastructure components such as `StartupLogger`. Configuration lives in `src/main/resources/`. Tests mirror the application package under `src/test/java/org/example/`. Generated classes, reports, and executable JARs belong in `target/`; do not commit them. There are no frontend assets currently.

## Build, Test, and Development Commands

Use JDK 21 and the included Maven Wrapper (Maven 3.9.15) by default; no Maven installation is required. The wrapper does not install a JDK. Maven 3.9.x is supported; Maven Enforcer checks Java and Maven versions during validate. No operating system, JDK vendor, or JDK patch version is prescribed. On Windows, use `mvnw.cmd` instead of `./mvnw`.

- `./mvnw clean verify`: clean stale artifacts, run tests, package the executable JAR, and check Java formatting.
- `./mvnw test`: run the test suite without packaging.
- `./mvnw spotless:apply`: format production and test Java sources.
- `./mvnw spotless:check`: check Java formatting without changing files.
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`: run locally with Swagger enabled.
- `java -jar target/spring-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`: run the packaged application.

The default port is 8080. `ExampleController` is registered only with the `dev` profile; default and prod must not expose `/examples/**`. In dev, `GET /examples/success` returns JSON containing the message `Hello World!`.

## Coding Style & Naming Conventions

Use Spotless with the pinned Google Java Format AOSP style: four-space indentation, opening braces on the declaration line, PascalCase class names, camelCase methods and fields, and lowercase package names. Use two-space YAML indentation. `.editorconfig` requires UTF-8, LF endings, and a final newline. Spotless controls Java layout and import ordering; run `./mvnw spotless:apply` before submitting changes. Formatting checks run during `verify`. No separate lint plugin is configured. Keep startup logic in components rather than expanding `Application.main`. Let the Boot parent manage dependency versions; declare additional library versions in POM properties.

## Testing Guidelines

Use JUnit Jupiter, Spring Boot Test, and MockMvc. Name test classes `*Test` and use descriptive method names. Cover changed HTTP behavior and configuration behavior, particularly Swagger availability in default, `dev`, and `prod` environments. Add tests for new endpoints. Tests that exercise example endpoints must explicitly activate dev; default/prod isolation tests must not import the example controller to bypass its profile. No numeric coverage threshold is configured. Run `./mvnw clean verify` before submitting changes.

## Commit & Pull Request Guidelines

Run `./mvnw clean verify` locally before committing code or creating a pull request.

History uses descriptive imperative subjects such as “Add SpringDoc configuration and tests” and “Refactor logging to StartupLogger class.” Follow that style and keep commits focused. Pull requests should explain the change, relevant configuration effects, and validation results; link related issues when applicable. Include response examples for API changes.

## Configuration Tips

Swagger is disabled by default and enabled explicitly through the `dev` profile at `/swagger-ui.html`. Do not activate `dev` for production deployments. Keep credentials out of committed YAML; supply them through external configuration or environment variables.

## Logging Guidelines

Use SLF4J with Boot's default Logback. Default and dev profiles use text console logs; prod uses native Logstash JSON console logs. Do not add file logging by default.

- ERROR: unexpected system exceptions, unrecoverable failures, and program defects requiring investigation.
- WARN: recoverable abnormal conditions, such as external-service retries or unusual system states.
- INFO: important application lifecycle and business events, such as startup or completion of significant tasks.
- DEBUG: development troubleshooting without sensitive data.
- TRACE: very fine-grained diagnostics; disabled by default.

Expected `BusinessException` and framework errors mapped to `RequestError` must not log ERROR stacks by default. Ordinary 400/404 responses are not automatically system failures. Unexpected system exceptions returning 500 must log ERROR with a stack trace. Controller return-value validation failures are system errors. Record a full stack once at the designated handling boundary; do not repeat the same exception across Controller, Service, and global handlers.

Use SLF4J parameterized logging, for example `log.info("User created: userId={}", userId)`. Do not actively log passwords, access/refresh tokens, Authorization headers, Cookie/Session credentials, API keys, private keys, other secrets, sensitive request/response data, or unreviewed sensitive business fields. Do not serialize entire unreviewed request objects or directly log raw request URIs/query strings in global exception logs.

Full Throwables can still contain sensitive information in their messages and stacks: removing a URI from the log context does not guarantee secret-free logs. Keep secrets out of internally controlled exception messages. Assess third-party exceptions before logging their original Throwable; where necessary, log a controlled summary and exception type. Do not blanket-suppress system exception stacks or introduce a global regex masking framework, new logging service abstraction, or logging-specific exception hierarchy.

## Request ID Guidelines

HTTP requests use a server-generated UUID v4, response header `X-Request-ID`, and MDC key `requestId`. Never trust or echo a client-provided ID. Keep the ID consistent throughout a synchronous request and reuse the request attribute during ERROR/ASYNC redispatches. Restore any previous MDC value in finally, or remove the key if absent on entry; preserve unrelated MDC fields. Arbitrary async task threads do not inherit this context automatically.

Business logs need not concatenate requestId; the configured log output includes MDC. Do not treat requestId as authentication, add it to `ErrorResponse`, or use it instead of business audit records. Do not add access logging merely to produce an event for every ID.

## Template Scope

Preserve the existing error-code, internationalization, logging, and Request ID conventions when adding business APIs. Introduce databases, authentication, caches, messaging, and deployment infrastructure only when required by the business; do not add them to the template speculatively.
