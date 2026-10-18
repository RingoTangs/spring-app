# Spring App

English | [简体中文](README.zh-CN.md)

A lightweight Spring Boot REST API template with common backend foundations, without prescribing a database, authentication system, or deployment architecture.

**Stack:** Java 21 · Spring Boot 4.1.1 · Maven Wrapper 3.9.15

## Core Features

- Spring MVC and Jakarta Validation for HTTP APIs and request validation.
- Unified error codes and BusinessException, with English and Chinese error responses.
- Development-only Swagger UI and OpenAPI documentation.
- SLF4J + Logback, production JSON console logs, and Request ID correlation.
- JUnit, MockMvc, Spotless, and GitHub Actions CI.

## Quick Start

Install JDK 21. The included Wrapper downloads Maven; a separate Maven installation is not required.

```bash
./mvnw clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The default port is **8080**. With dev enabled:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- Example endpoint: <http://localhost:8080/examples/success>

On Windows, use `mvnw.cmd` instead of `./mvnw` (`.\mvnw.cmd` in PowerShell).

## Environment Profiles

| Profile | Swagger / Example endpoints | Console logs |
|---|---|---|
| Default | Disabled | Text |
| dev | Enabled | Text |
| prod | Disabled | JSON |

`ExampleController` and Swagger are enabled only in dev. Never activate dev in production, including together with prod. Production logs use the Logstash JSON format and are written to the console; log files are not created by default.

## API Conventions

Unified error responses contain only `code` and `message`:

```json
{
  "code": "COMMON_NOT_FOUND",
  "message": "Resource not found"
}
```

`ErrorCode` defines the public error code, HTTP status, and translation key. Use `BusinessException` for expected business errors. `Accept-Language: zh-CN` selects Chinese and `en-US` selects English; missing or unsupported languages fall back to English. Unexpected system exceptions return safe messages without exposing internal details.

The server generates a Request ID and returns it in `X-Request-ID` for successful responses and errors handled by the application. Use this header to correlate existing logs; client-provided IDs are ignored. It is not an authentication credential. Cross-origin browser clients require this header to be exposed under the application's CORS policy.

See [AGENTS.md](AGENTS.md) for development and logging conventions. Run `./mvnw clean verify` before committing or opening a PR; CI runs the same checks on main pushes, PRs targeting main, and manual dispatch.

## Create a Project from This Template

1. Use GitHub's **Use this template** option when enabled, or copy the code into a new repository.
2. Update project coordinates, `name`, and `description` in `pom.xml` (preserve the Boot parent), `spring.application.name` in `src/main/resources/application.yml`, and the `org.example` package. Rename source and test packages together, including imports and `package-info.java`; keep `Application` at an appropriate component-scan root.
3. Replace or remove the dev examples as needed, updating related tests, OpenAPI assertions, and README content.
4. Run `./mvnw clean verify`, then start with the intended profile and check its behavior. This repository does not automatically rename your project.

Add databases, authentication/authorization, caches, messaging, audit storage, Docker deployment, and distributed tracing only as required by your business; these are not bundled with the template.
