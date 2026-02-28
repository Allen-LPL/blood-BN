# AGENTS.md

Guidance for coding agents working in this repository.

## Scope

- This repository is a Java monorepo based on Maven, Spring Boot 2.7, JDK 8.
- Primary modules enabled here: `yudao-server`, `yudao-module-system`, `yudao-module-infra`, and shared framework modules under `yudao-framework`.
- Root aggregator is `pom.xml` (packaging `pom`) with module declarations.

## Repository Map

- `pom.xml` - root multi-module build and plugin management.
- `yudao-server/` - runnable server container module.
- `yudao-module-system/` - system business module.
- `yudao-module-infra/` - infrastructure module (codegen, job, file, config, logs).
- `yudao-framework/` - shared starters and common components.
- `sql/` - schema scripts for multiple databases.
- `.github/workflows/maven.yml` - CI build command reference.
- `script/jenkins/Jenkinsfile` - Jenkins build/deploy reference.

## Toolchain Baseline

- Java: `1.8` (`pom.xml`).
- Build system: Maven (no Gradle files found).
- Test engine: JUnit 5 via Maven Surefire plugin (`maven-surefire-plugin` configured in root `pom.xml`).
- Lombok is heavily used (`lombok.config` present).

## Build, Test, and Run Commands

Use repo root as working directory unless noted.

### Canonical Commands (from repo configs)

- Full package (skip tests), CI-style:
  - `mvn -B package --file pom.xml -Dmaven.test.skip=true`
  - Source: `.github/workflows/maven.yml`
- Full package (skip tests), Jenkins-style:
  - `mvn clean package -Dmaven.test.skip=true`
  - Source: `script/jenkins/Jenkinsfile`
- Dockerized package (skip tests):
  - `docker run ... maven mvn clean install package '-Dmaven.test.skip=true'`
  - Source: `script/docker/Docker-HOWTO.md`

### Recommended Local Commands

- Build all modules without tests:
  - `mvn clean package -Dmaven.test.skip=true`
- Build all modules with tests enabled:
  - `mvn clean test`
- Build only server + required upstream modules:
  - `mvn -pl yudao-server -am package -Dmaven.test.skip=true`
- Run server jar after packaging:
  - `java -jar yudao-server/target/yudao-server.jar`

### Single-Test Commands (important)

Surefire is configured, so use standard Maven test selectors.

- Run one test class:
  - `mvn -pl <module> -Dtest=ClassNameTest test`
- Run one test method:
  - `mvn -pl <module> -Dtest='ClassNameTest#methodName' test`
- Run multiple classes:
  - `mvn -pl <module> -Dtest=ClassATest,ClassBTest test`

Notes:

- Quote `Class#method` in zsh to avoid shell parsing issues.
- Prefer `-pl <module> -am` when dependencies from sibling modules are needed.
- This repository currently has very few concrete test classes under `src/test/java`; generated templates exist for service tests.

## Lint / Formatting Reality

- No explicit Checkstyle/Spotless/PMD/ESLint/Prettier config files were found in this repo.
- No dedicated lint command is defined in root Maven config.
- Therefore, use compilation + tests as quality gate, and match existing style exactly.

## Code Style Conventions (observed)

These are derived from existing Java source files in enabled modules.

### Imports

- Group imports by origin with blank lines between groups.
- Typical order pattern:
  1) project/internal imports (`cn.iocoder...`)
  2) third-party imports
  3) `javax.*` and `java.*`
  4) static imports at the end
- Static imports are used for constants/utilities, e.g. error codes and helper methods.
- Avoid introducing wildcard imports unless there is an established local precedent.

### Formatting

- 4-space indentation, K&R braces (`public class X {`).
- Keep line width readable; wrap long method calls across lines with aligned continuation.
- Keep short guard clauses early.
- Use inline comments sparingly, mostly for business context or step numbering.

### Naming and Structure

- Class suffix conventions are important and pervasive:
  - `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, `*Convert`
  - `*DO` (data object), `*ReqVO` / `*RespVO` (request/response view objects)
- Controller packages often include endpoint scope (e.g. `controller/admin/...`).
- Use explicit business verbs for service methods (`createX`, `updateX`, `deleteX`, `getXPage`).

### Types and APIs

- Use boxed numeric IDs (`Long`, `Integer`) rather than primitives for nullable fields.
- API responses should use `CommonResult<T>` and helper constructors (`success`, `error`).
- Bean mapping usually goes through utility conversion helpers (`BeanUtils.toBean(...)`) or `*Convert` classes.
- Lombok is expected (`@Slf4j`, `@Data`, chaining setters via `lombok.accessors.chain=true`).

### Error Handling

- For business errors, prefer `ServiceException` via `ServiceExceptionUtil.exception(...)`.
- Reuse existing error code enums instead of ad-hoc messages.
- Let `GlobalExceptionHandler` translate exceptions to `CommonResult` responses.
- Avoid swallowing exceptions silently; if caught, log with context and preserve behavior.

### Transactions and Validation

- Use `@Transactional(rollbackFor = Exception.class)` on mutating service operations.
- Use Bean Validation annotations on request objects and `@Valid` in controller methods.
- Enforce uniqueness/existence checks through dedicated validation helper methods before writes.

## Testing Conventions

- Framework: JUnit 5 (`org.junit.jupiter`) and Mockito patterns are used.
- Generated service tests use `BaseDbUnitTest` and assert helpers from framework test utilities.
- Prefer naming tests as `testAction_condition_expected`.
- Keep test setup in clear sections: arrange, act, assert.

## Agent Workflow Rules

- Make minimal, localized changes first; follow existing package/module boundaries.
- Do not introduce new frameworks or formatting tools without explicit request.
- When adding functionality, mirror naming and layering already used in nearby modules.
- Validate with module-targeted Maven commands before broad full-repo commands.
- Never commit generated artifacts under `target/`.

## Cursor / Copilot Rules

- `.cursorrules`: not found.
- `.cursor/rules/`: not found.
- `.github/copilot-instructions.md`: not found.

If these files are added later, treat them as higher-priority constraints and update this AGENTS file.
