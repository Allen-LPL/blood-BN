# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Blood Collection/Supply Management System based on the Yudao (芋道) framework - a Java monorepo using Spring Boot 2.7 + JDK 8 + Maven.

**Active modules:** `yudao-server`, `yudao-module-system`, `yudao-module-infra`, `yudao-framework`

## Build & Run Commands

```bash
# Build all modules (skip tests)
mvn clean package -Dmaven.test.skip=true

# Build server + dependencies only
mvn -pl yudao-server -am package -Dmaven.test.skip=true

# Run server
java -jar yudao-server/target/yudao-server.jar

# Run single test class
mvn -pl <module> -Dtest=ClassNameTest test

# Run single test method (quote in zsh)
mvn -pl <module> -Dtest='ClassNameTest#methodName' test
```

## Architecture

**Layered 3-tier:** Controller → Service → Mapper (MyBatis Plus)

**Naming conventions (critical):**
- `*Controller` - REST endpoints
- `*Service` / `*ServiceImpl` - Business logic
- `*Mapper` - MyBatis data access (extends `BaseMapperX` + uses `LambdaQueryWrapperX`)
- `*DO` - Data Object (entity, maps to DB table)
- `*ReqVO` / `*RespVO` - Request/Response view objects
- `*Convert` - Bean mapping (MapStruct or `BeanUtils.toBean()`)

**API responses:** All use `CommonResult<T>` wrapper with `code`, `msg`, `data` fields.

**Error handling:** Use `ServiceException` via `ServiceExceptionUtil.exception(ErrorCodeEnum)`. Reuse existing error code enums.

## Multi-Tenancy

Enabled by default. Tables get automatic `tenant_id` WHERE clause injection.

**For global tables (no tenant isolation):**
- Annotate DO with `@TenantIgnore`
- Do NOT extend `BaseDO/TenantBaseDO`

Blood collection/supply tables (`blood_collection_fact`, `blood_supply_fact`) use `@TenantIgnore`.

## Date/Time Handling

**Known issue:** `TimestampLocalDateTimeDeserializer` only accepts epoch milliseconds in POST JSON bodies. `@JsonFormat` annotations are ignored for deserialization.

**Workarounds:**
- GET requests: Use `@DateTimeFormat` (works with query params)
- POST requests: Either send epoch milliseconds, or modify the deserializer to handle strings

## Key Directories

```
yudao-server/               # Main application entry point
yudao-module-infra/         # Infrastructure + blood APIs
  └─ controller/admin/blood/  # Blood REST controllers
  └─ service/blood/           # Blood services
  └─ dal/mysql/blood/         # Blood mappers
  └─ dal/dataobject/blood/    # Blood DOs
yudao-framework/            # Shared starters and utilities
sql/                        # DB schemas (MySQL, PostgreSQL, Oracle, etc.)
```

## Adding a New API

1. **Create DO** in `dal/dataobject/` - `@TableName`, `@TableId`, use boxed types
2. **Create Mapper** in `dal/mysql/` - extend `BaseMapperX<DO, Long>`
3. **Create Service/ServiceImpl** in `service/` - use `@Transactional(rollbackFor = Exception.class)`
4. **Create VOs** in `controller/admin/.../vo/` - `*ReqVO` for requests, `*RespVO` for responses
5. **Create Controller** in `controller/admin/` - return `CommonResult.success(data)`

## Code Style

- 4-space indentation, K&R braces
- Import groups: project → third-party → javax/java → static
- Use `@Valid` on controller methods, Bean Validation annotations on VOs
- Test naming: `testAction_condition_expected`
- No explicit lint tools; match existing style

## Configuration

- Default profile: `local` (see `application-local.yaml`)
- Admin API prefix: `/admin-api`
- Swagger UI: `/swagger-ui`
