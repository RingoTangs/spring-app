# Spring App

English | [简体中文](README.zh-CN.md)

A lightweight Spring Boot REST API development template with an HTTP API foundation, request validation, unified error handling, English and Chinese error messages, development-only Swagger, logging conventions, Request IDs, automated tests, and GitHub Actions CI.

Stack: Java 21, Spring Boot 4.1.1, Spring MVC, Jakarta Validation, SpringDoc OpenAPI, SLF4J + Logback, JUnit/MockMvc, Spotless, and Maven Wrapper 3.9.15.

## Development Requirements

- JDK 21 is required. Any operating system, JDK vendor, and JDK 21 patch version may be used.
- Maven 3.9.x is supported. Use the included Wrapper, pinned to Maven 3.9.15; no Maven installation is required.
- The Wrapper does not install a JDK. Its first run requires internet access to download Maven into the user cache; do not commit cached files.
- Enforcer checks Java and Maven versions during `validate`. Source files, resources, and reports use UTF-8.

```sh
java -version
./mvnw -version
```

## Quick Start

```sh
./mvnw clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The default port is 8080. With dev enabled, visit:

- Example: `http://localhost:8080/examples/success`
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

On Windows, replace `./mvnw` with `mvnw.cmd`, or `.\mvnw.cmd` in PowerShell. For example: `mvnw.cmd clean verify`.

## Profiles

| Profile | Example endpoints `/examples/**` | Swagger / OpenAPI | Console logs |
|---|---|---|---|
| Default | Not registered | Disabled | Text |
| dev | Registered | Enabled | Text |
| prod | Not registered | Disabled | Logstash JSON |

`ExampleController` is registered only in dev. With the default or prod profile, requests to the example paths return 404 through normal MVC handling. Never enable dev in production, including alongside prod. Implement business endpoints independently of the example controller.

Start in production mode from a POSIX shell:

```sh
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Create a Project from This Template

1. If the GitHub **Template repository** option is enabled, select **Use this template → Create a new repository**. Otherwise, a maintainer can enable it under **Settings → General → Template repository**. You can also copy the code and initialize your own repository.
2. Update the project's `groupId`, `artifactId`, `name`, and `description` in `pom.xml`, preserving the Spring Boot parent coordinates. Update documentation and JAR launch commands that depend on these names.
3. Rename `org.example` to your business root package. Update directories, package declarations, imports, related tests and configuration, and every `package-info.java` under both `src/main/java` and `src/test/java`. Keep `Application` in a root package that can scan your business components, and confirm that infrastructure such as Request ID configuration remains discoverable.
4. Update `spring.application.name` in `src/main/resources/application.yml`.
5. Keep or remove the dev-only `ExampleController` as needed. If removed, update tests, OpenAPI assertions, and documentation that reference its paths, while retaining meaningful error-handling and Request ID tests.
6. Update the README's project description, names, API examples, and instructions. Run `./mvnw clean verify`, then start the application with the intended profile and check its behavior.

This repository keeps `spring-app` and `org.example` as template examples. Project coordinates and package names are not replaced automatically.

## Unified Error Handling and Internationalization

`ErrorResponse` contains only `code` and `message`. Each part of `ErrorCode` has a separate responsibility:

```text
code       = COMMON_NOT_FOUND          # Stable public API error identifier
status     = 404                       # HTTP status
messageKey = error.common.not_found    # Internal translation resource identifier
```

CommonError codes use the `COMMON_` prefix, RequestError codes use `REQUEST_`, and business modules use prefixes such as `USER_`, `ORDER_`, or `PAYMENT_`. Message keys follow `error.<module>.<error_name>`. Add both English and Chinese resources for each new error.

Use `BusinessException.of(CommonError.NOT_FOUND)`. For dynamic messages, use `BusinessException.of(error, "alice")` with `{0}`, `{1}` placeholders. A single String argument is a template argument, not a complete message override. Exceptions do not translate messages in advance: `getMessage()` is diagnostic, and final messages are resolved at the HTTP response boundary. Argument arrays are defensively copied; prefer immutable argument values.

`Accept-Language: zh-CN` selects Chinese, and `en-US` selects English. Missing headers or unmatched translation resources fall back to English, independently of the system Locale. UTF-8 resources are stored in `messages.properties` (English default) and `messages_zh.properties`.

Enable dev before running these examples:

```sh
curl -H 'Accept-Language: zh-CN' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"资源不存在"}
curl -H 'Accept-Language: en-US' http://localhost:8080/examples/business-error
# HTTP 404: {"code":"COMMON_NOT_FOUND","message":"Resource not found"}
```

Missing or unformattable messages use a safe generic error message while preserving the original code and status. If the generic resource is also unavailable, the fallback is `Internal server error`. Unexpected exceptions return HTTP 500, `COMMON_INTERNAL_SERVER_ERROR`, and a localized generic message without exposing exception details to clients.

## Logging and Request IDs

The application uses Spring Boot's default SLF4J + Logback setup. The prod profile uses native structured logging to emit Logstash JSON. Log files are not created by default; Docker deployments can use stdout/stderr with collection handled by an external platform.

Unexpected system exceptions are logged once at the handling boundary at ERROR level with a full stack trace. Expected business exceptions and request parameter errors do not log ERROR stacks by default. Ordinary 400/404 responses are not automatically system failures.

Do not actively log passwords, tokens, Authorization headers, Cookie/Session credentials, API keys, private keys, or unreviewed sensitive fields. Do not dump entire request/response objects. Throwable messages and stacks may still contain sensitive data: keep secrets out of internal exceptions, and assess whether third-party exceptions should be logged as raw Throwables or as controlled summaries with exception types. See AGENTS.md for detailed conventions.

Every new request entering the application Filter receives a server-generated UUID v4. Client-provided `X-Request-ID` values are ignored, and the generated ID is returned in that response header. This applies to successful responses and errors handled by the application without adding fields to business JSON.

With dev enabled:

```sh
curl -i http://localhost:8080/examples/success
```

Example response header:

```http
X-Request-ID: 7d9c25a8-f03b-4e61-8d12-10e847ae6b02
```

Frontends can display or save an issue reference using `response.headers.get("X-Request-ID")`; developers can use it to search existing logs. The MDC key is `requestId`, included automatically in request logs in default/dev text output and prod JSON output. Startup logs normally have no ID. Successful requests do not automatically generate access logs.

REQUEST, ERROR (including nested ERROR), and ASYNC redispatches reuse the same ID. Each dispatch clears or restores MDC on exit. Context is not automatically propagated to Callable, thread pools, or CompletableFuture, and distributed traceId/spanId support is not provided. Requests that never enter the Filter are not guaranteed an application ID, and headers cannot be added after the response is committed.

No global CORS policy is configured. For cross-origin deployments, add `X-Request-ID` to `exposedHeaders` under the appropriate origin and credential policy; do not allow all origins by default just to expose this header. A Request ID is not an identity credential and must not be used for authorization or as a replacement for business audit records.

## Tests and CI

```sh
./mvnw test
./mvnw spotless:apply
./mvnw spotless:check
./mvnw clean verify
```

Run `./mvnw clean verify` before committing or creating a PR. It covers Enforcer, compilation, tests, Boot packaging, and Spotless checks. Add tests for new business endpoints. Tests using example endpoints explicitly enable dev; default/prod tests verify isolation.

CI runs on pushes to main, PRs targeting main, and manual dispatch. It uses Ubuntu, Temurin JDK 21, the Maven Wrapper, and Maven caching. Its core command is `./mvnw --batch-mode --no-transfer-progress clean verify`. View results and logs under **Actions → CI → Build and Test**. Once the workflow is on the default branch, use **Run workflow** to trigger it manually. No additional Secrets are required.

## Extend as Needed

The template does not include a database, ORM/SQL framework, authentication/authorization, Redis, messaging, audit-log storage, Docker deployment configuration, or distributed tracing. Add these components when required by your business; none is a prerequisite for running this template.
