# Repository Guidelines

## Project Structure & Module Organization

This repository is a single-module Spring Boot 3.5.16 Web template using Java 21 and Maven. Production code lives in `src/main/java/org/example/`: `Application` is the minimal entry point, `controller/` contains HTTP endpoints, and `config/` contains infrastructure components such as `StartupLogger`. Configuration lives in `src/main/resources/`. Tests mirror the application package under `src/test/java/org/example/`. Generated classes, reports, and executable JARs belong in `target/`; do not commit them. There are no frontend assets currently.

## Build, Test, and Development Commands

Use JDK 21 and an installed Maven; this repository has no Maven Wrapper.

- `mvn clean verify`: clean stale artifacts, run tests, and package the executable JAR.
- `mvn test`: run the test suite without packaging.
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev`: run locally with Swagger enabled.
- `java -jar target/spring-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`: run the packaged application.

The default port is 8080. `GET /examples/success` returns JSON containing the message `Hello World!`.

## Coding Style & Naming Conventions

Follow existing Java style: four-space indentation, opening braces on the declaration line, PascalCase class names, camelCase methods and fields, and lowercase package names. Use two-space YAML indentation. `.editorconfig` requires UTF-8, LF endings, and a final newline. No formatter or lint plugin is configured. Keep startup logic in components rather than expanding `Application.main`. Let the Boot parent manage dependency versions; declare additional library versions in POM properties.

## Testing Guidelines

Use JUnit Jupiter, Spring Boot Test, and MockMvc. Name test classes `*Test` and use descriptive method names. Cover changed HTTP behavior and configuration behavior, particularly Swagger availability in default, `dev`, and `prod` environments. No numeric coverage threshold is configured. Run `mvn clean verify` before submitting changes.

## Commit & Pull Request Guidelines

History uses descriptive imperative subjects such as “Add SpringDoc configuration and tests” and “Refactor logging to StartupLogger class.” Follow that style and keep commits focused. Pull requests should explain the change, relevant configuration effects, and validation results; link related issues when applicable. Include response examples for API changes.

## Configuration Tips

Swagger is disabled by default and enabled explicitly through the `dev` profile at `/swagger-ui.html`. Do not activate `dev` for production deployments. Keep credentials out of committed YAML; supply them through external configuration or environment variables.
