# Blood Collection Fact - Admin APIs

## 结论（推荐落地方案）

本仓库是典型的 yudao/ruoyi-vue-pro 后端结构（Spring Boot + MyBatis-Plus），分页查询通常按 `*Controller` → `*Service` → `*Mapper`（`BaseMapperX` + `LambdaQueryWrapperX`）的方式实现。

针对 `blood_collection_fact`（大字段数 + 事实表场景），建议落地 3 类接口：

1) 列表分页查询：`GET /admin-api/infra/blood-collection-fact/page`

2) 检索查询：复用分页接口的筛选能力（精确/范围/模糊），必要时补一个“快速检索”参数（例如 donationCode/archiveId）。

3) 聚合查询：`POST /admin-api/infra/blood-collection-fact/aggregate`，采用“受限 DSL”（groupBy/metrics 白名单枚举），强制时间范围 + 限流（limit、groupBy 数量、最大跨度）来保证性能与安全。

多租户重要提示：当前表 DDL **没有 `tenant_id`、也没有 `create_time/update_time/deleted`**。

- 如果项目启用了多租户（本仓库默认启用），直接把该表做成普通 DO 会触发租户拦截器拼接 `tenant_id` 条件，导致 SQL 报错。
- 解决方案二选一：
  - 方案 A（多租户隔离）：给表增加 `tenant_id`，并按框架规范补齐基础字段（或至少 `tenant_id`）；DO 继承 `TenantBaseDO`。
  - 方案 B（单租户/全局表，推荐作为默认落地）：DO 标注 `@TenantIgnore`，并且 **不要继承** `BaseDO/TenantBaseDO`，避免框架默认字段映射。

下文 API/Model 设计默认采用 **方案 B**（`@TenantIgnore`），若要支持多租户隔离，按“方案 A 改造点”调整即可。

---

## 表结构与 Model（DO）

表：`blood_collection_fact`

字段建议映射（Java 类型）：

- `id` → `Long`
- `collection_department` → `String`
- `collection_site` → `String`
- `organization_mode` → `String`
- `donation_type` → `String`
- `archive_id` → `String`
- `gender` → `String`
- `age` → `Integer`
- `org_unit_name` → `String`
- `unit_property` → `String`
- `system_name` → `String`
- `unit_level` → `String`
- `parent_unit` → `String`
- `unit_admin_region` → `String`
- `archive_created_date` → `java.time.LocalDateTime`
- `registration_time` → `java.time.LocalDateTime`
- `precheck_time` → `java.time.LocalDateTime`
- `blood_collection_time` → `java.time.LocalDateTime`
- `full_volume_flag` → `String`
- `insufficient_reason` → `String`
- `donation_code` → `String`
- `precheck_result`（text）→ `String`
- `precheck_fail_items`（text）→ `String`
- `archive_blood_type` → `String`
- `precheck_blood_type` → `String`
- `blood_volume` → `String`
- `base_unit_value` → `java.math.BigDecimal`
- `recheck_result`（text）→ `String`
- `recheck_fail_items`（text）→ `String`
- `source_file` → `String`
- `sheet_name` → `String`
- `source_row_num` → `Integer`
- `load_batch_id` → `String`
- `ingested_at` → `java.time.LocalDateTime`

DO 约束：

- 不继承 `BaseDO/TenantBaseDO`（因为表没有 `create_time/update_time/deleted/tenant_id`）
- 加 `@TenantIgnore`（避免租户插件拼接 `tenant_id`）
- `@TableName("blood_collection_fact")` + `@TableId`

---

## 列表/检索查询接口

### 1) 分页列表

`GET /admin-api/infra/blood-collection-fact/page`

权限：`infra:blood-collection-fact:query`

返回：`CommonResult<PageResult<BloodCollectionFactRespVO>>`

分页请求 VO（建议）：`BloodCollectionFactPageReqVO extends PageParam`

筛选字段建议（按“常用 + 可索引 + 可控”优先）：

- 精确：`archiveId`, `donationCode`, `loadBatchId`, `collectionDepartment`, `collectionSite`, `organizationMode`, `donationType`, `gender`, `fullVolumeFlag`, `archiveBloodType`, `precheckBloodType`, `unitAdminRegion`
- 范围：`age`（拆成 `ageMin/ageMax`），以及 `registrationTime[]/precheckTime[]/bloodCollectionTime[]/ingestedAt[]`
- 模糊（谨慎）：`orgUnitName`, `systemName`, `parentUnit`（仅在确有需求且数据量可接受时开启）

排序建议：`blood_collection_time DESC, id DESC`（若采血时间为空，则退化 `precheck_time DESC` 或 `id DESC`）。

说明：所谓“检索查询”，通常就是分页接口的组合筛选；如果前端需要“快速检索”，可增加一个 `keyword`，但要明确只作用于少数列（例如 donationCode/archiveId），避免全表 LIKE。

---

## 聚合查询接口

### 2) 聚合查询

`POST /admin-api/infra/blood-collection-fact/aggregate`

权限：`infra:blood-collection-fact:aggregate`

返回：`CommonResult<BloodCollectionFactAggRespVO>`

为什么用 POST：聚合请求通常包含 `groupBy/metrics/filter` 的结构化组合，使用 JSON body 更清晰。

### 聚合 DSL（受限白名单）

聚合请求 VO（建议）：`BloodCollectionFactAggReqVO`

- `filter: BloodCollectionFactQueryReqVO`（复用分页筛选字段，但不含 pageNo/pageSize）
- `groupBy: List<GroupByFieldEnum>`（最多 2 个）
- `metrics: List<MetricSpec>`（最多 5 个）
- `orderBy: OrderBySpec`（仅允许按 metrics 别名或 groupBy key 排序）
- `limit: Integer`（默认 100，最大 1000）

强制约束（服务端校验）：

- 若未传任何时间范围：默认使用最近 3 个月的 `bloodCollectionTime` 作为查询窗口（避免全表扫描）
- `groupBy.size <= 2`
- `limit <= 1000`
- `metrics` 只能对数值列做 SUM/AVG（例如 `base_unit_value`），对字符串列只允许 COUNT/COUNT_DISTINCT（且 DISTINCT 列需要白名单）

### groupBy 白名单（建议）

可选维度（举例，按业务可裁剪）：

- `COLLECTION_DEPARTMENT`（采血部门）
- `COLLECTION_SITE`（采血地点）
- `ORGANIZATION_MODE`（组织方式）
- `DONATION_TYPE`（献血类型）
- `GENDER`
- `UNIT_ADMIN_REGION`（行政区）
- `SYSTEM_NAME`
- `UNIT_PROPERTY`
- `UNIT_LEVEL`
- `FULL_VOLUME_FLAG`
- `ARCHIVE_BLOOD_TYPE`
- `PRECHECK_BLOOD_TYPE`

时间维度建议单独做：

- `TIME_DAY` / `TIME_MONTH`（基于 `blood_collection_time` 或 `precheck_time` 的日/月桶）

说明：时间桶 `DATE_FORMAT(...)` 会让索引失效或产生临时表，若日/月趋势是核心诉求，建议增加派生列（例如 `blood_collection_date`）并建立索引。

### metrics 白名单（建议）

- `COUNT_ROWS`：`COUNT(1)`
- `COUNT_DONATION_CODE`：`COUNT(donation_code)`（视业务含义）
- `COUNT_DISTINCT_ARCHIVE_ID`：`COUNT(DISTINCT archive_id)`（如需）
- `SUM_BASE_UNIT_VALUE`：`SUM(base_unit_value)`
- `AVG_AGE`：`AVG(age)`

---

## 权限与路由

本仓库 Admin API 前缀由 `yudao.web.admin-api.prefix=/admin-api` 统一配置。

如果 Controller 写：

- `@RequestMapping("/infra/blood-collection-fact")`

则实际对外路径为：

- `/admin-api/infra/blood-collection-fact/...`

建议权限字符串（按 infra 模块风格）：

- 查询：`infra:blood-collection-fact:query`
- 聚合：`infra:blood-collection-fact:aggregate`

如果未来拆分成独立业务模块（例如 `yudao-module-blood`），则可改为 `blood:*` 前缀。

---

## 性能与索引建议（结合当前 DDL）

当前 DDL 已有：

- `idx_archive_id (archive_id)`
- `idx_donation_code (donation_code)`
- `idx_precheck_time (precheck_time)`
- `idx_batch (load_batch_id)`

建议补充的索引需要由“实际查询模式”决定（不要盲目给每列加索引）。常见聚合/筛选会需要时间 + 维度的联合索引，例如：

- `(precheck_time, collection_site)`：按初筛时间过滤并按采血点聚合
- `(precheck_time, collection_department)`：按部门聚合
- `(load_batch_id, source_row_num)`：导入批次定位问题行

注意：多列联合索引的列顺序应以 where 条件的选择性为主（一般先时间再维度，或先批次再行号）。

---

## 方案 A（多租户隔离）改造点

如果确定该事实表需要多租户隔离：

1) DDL 增加 `tenant_id bigint NOT NULL DEFAULT 0`（并考虑联合索引前缀）
2) 视是否需要逻辑删除/审计字段：增加 `create_time/update_time/creator/updater/deleted`
3) DO 继承 `TenantBaseDO`（或至少确保租户插件能够识别 tenant 字段）
4) 去掉 `@TenantIgnore`

---

## 待确认点（会影响最终接口形态）

1) 聚合查询是“固定仪表盘”还是“前端可自由选维度/指标”？
   - 默认按本文推荐：前端自由选，但在后端强白名单约束。
2) 是否需要多租户隔离（tenant_id）？
   - 默认按单租户/全局表处理（`@TenantIgnore`）。

---

## BUG: `bloodCollectionTime` 反序列化为 1970-01-01

### 现象

前端发送 POST `/admin-api/infra/blood-collection-fact/aggregate`：

```json
{
  "filter": {
    "bloodCollectionTime": ["2025-08-25 00:00:00", "2026-02-25 23:59:59"]
  },
  "groupBy": [30],
  "metrics": [
    {"op": 3, "field": 4, "alias": "totalVolume"},
    {"op": 1, "field": 1, "alias": "totalCount"}
  ],
  "limit": 30
}
```

后端 `BloodCollectionFactQueryReqVO.bloodCollectionTime` 接收到：

```
[1970-01-01T08:00, 1970-01-01T08:00]
```

两个日期值全部变成 Unix 纪元零点（UTC+8）。

### 根因分析

#### 反序列化链路

```
前端 JSON string "2025-08-25 00:00:00"
  ↓ @RequestBody → Jackson ObjectMapper
  ↓ 查找 LocalDateTime 反序列化器
TimestampLocalDateTimeDeserializer（全局注册于 YudaoJacksonAutoConfiguration）
  ↓ p.getValueAsLong()
字符串 "2025-08-25 00:00:00" → 无法解析为 Long → 返回 0
  ↓ Instant.ofEpochMilli(0)
1970-01-01T00:00:00 UTC → ZoneId.systemDefault() (UTC+8)
  ↓
1970-01-01T08:00:00
```

#### 关键文件

| 文件 | 路径 | 作用 |
|------|------|------|
| YudaoJacksonAutoConfiguration | `yudao-framework/yudao-spring-boot-starter-web/.../jackson/config/` | 全局注册 `TimestampLocalDateTimeDeserializer` 处理所有 `LocalDateTime` |
| TimestampLocalDateTimeDeserializer | `yudao-framework/yudao-common/.../json/databind/` | **只**接受 epoch 毫秒（Long），调用 `p.getValueAsLong()` |
| TimestampLocalDateTimeSerializer | 同上 | 序列化时**会**检查 `@JsonFormat`（反射），有则输出字符串；无则输出 epoch |
| BloodCollectionFactQueryReqVO | `yudao-module-infra/.../blood/vo/` | `bloodCollectionTime` 标注了 `@JsonFormat` + `@DateTimeFormat` |

#### 序列化器 vs 反序列化器的不对称

**序列化器** `TimestampLocalDateTimeSerializer`：

```java
// 情况一：有 @JsonFormat → 反射读取 pattern → 输出字符串
if (field != null && field.isAnnotationPresent(JsonFormat.class)) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(jsonFormat.pattern());
    gen.writeString(formatter.format(value));
    return;
}
// 情况二：默认输出 epoch 毫秒
gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
```

**反序列化器** `TimestampLocalDateTimeDeserializer`：

```java
@Override
public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    // 不检查 @JsonFormat、不检查 token 类型、始终当 Long 处理
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getValueAsLong()), ZoneId.systemDefault());
}
```

> **根因：序列化器尊重 `@JsonFormat`，反序列化器完全忽略它。**

#### `@JsonFormat` 为什么没有覆盖全局反序列化器

Jackson 标准行为：`@JsonFormat(pattern=...)` 触发 `ContextualDeserializer.createContextual()` 返回格式感知的反序列化器。

但 `TimestampLocalDateTimeDeserializer`：
- 继承 `JsonDeserializer<LocalDateTime>`（不是 jsr310 的 `LocalDateTimeDeserializer`）
- **未实现** `ContextualDeserializer` 接口
- 被 `deserializerByType(LocalDateTime.class, ...)` 全局注册，优先级高于 `@JsonFormat`

所以 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 在反序列化时完全无效。

#### 其他日期字段为什么不报错

`BloodCollectionFactQueryReqVO` 中的其他日期字段（`registrationTime`、`precheckTime`、`ingestedAt`）：
- 只有 `@DateTimeFormat`，无 `@JsonFormat`
- 前端**未传值**（全部为 `null`），所以没有触发反序列化
- `@DateTimeFormat` 是 Spring MVC 注解，仅对 `@RequestParam` / 表单绑定生效，对 `@RequestBody` JSON 反序列化**无效**
- 如果前端对这些字段也传字符串日期，同样会得到 `1970-01-01T08:00`

#### `BloodCollectionFactPageReqVO` 为什么能正常工作

分页接口 `GET /page` 参数通过 query string 传递，Spring MVC 的 `@DateTimeFormat` 正确处理。
但 `/aggregate` 是 `@RequestBody` POST 请求，走 Jackson 反序列化，`@DateTimeFormat` 不生效。

### `@DateTimeFormat` vs `@JsonFormat` 适用场景

| 注解 | 适用场景 | 生效条件 |
|------|---------|---------|
| `@DateTimeFormat` | Spring MVC 参数绑定（`@RequestParam`, `@ModelAttribute`, query string） | GET 请求 / form 表单 |
| `@JsonFormat` | Jackson JSON 序列化/反序列化（`@RequestBody`） | POST/PUT JSON body |

在本项目中，由于 `TimestampLocalDateTimeDeserializer` 未实现 `ContextualDeserializer`，`@JsonFormat` 对反序列化实际上无效。

### 修复方案

#### 方案 A：修改反序列化器，同时支持字符串和数字（推荐）

修改 `TimestampLocalDateTimeDeserializer`，根据 token 类型自动选择解析方式：

```java
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 情况一：字符串 → 按默认格式解析
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (!text.isEmpty()) {
                return LocalDateTime.parse(text, DEFAULT_FORMATTER);
            }
        }
        // 情况二：数字 → 按 epoch 毫秒解析（保持原有行为）
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(p.getValueAsLong()), ZoneId.systemDefault());
    }
}
```

优点：向后兼容、改动小。缺点：默认格式硬编码。

#### 方案 B：实现 `ContextualDeserializer`（完整支持 `@JsonFormat`）

```java
public class TimestampLocalDateTimeDeserializer
        extends JsonDeserializer<LocalDateTime>
        implements ContextualDeserializer {

    public static final TimestampLocalDateTimeDeserializer INSTANCE =
            new TimestampLocalDateTimeDeserializer(null);
    private final DateTimeFormatter formatter;

    public TimestampLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                 BeanProperty property) {
        if (property != null) {
            JsonFormat ann = property.getAnnotation(JsonFormat.class);
            if (ann == null) ann = property.getContextAnnotation(JsonFormat.class);
            if (ann != null && !ann.pattern().isEmpty()) {
                return new TimestampLocalDateTimeDeserializer(
                        DateTimeFormatter.ofPattern(ann.pattern()));
            }
        }
        return this;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (!text.isEmpty()) {
                DateTimeFormatter fmt = (formatter != null) ? formatter
                        : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(text, fmt);
            }
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(p.getValueAsLong()), ZoneId.systemDefault());
    }
}
```

优点：完全兼容 Jackson `@JsonFormat` 机制、与序列化器对称。缺点：改动稍大。

#### 方案 C：前端改为发送 epoch 毫秒

前端将 `"2025-08-25 00:00:00"` 转换为 `1724544000000` 后发送。无需改后端，但日志可读性差。

### 涉及文件清单

| 文件 | 说明 |
|------|------|
| `yudao-framework/yudao-common/.../TimestampLocalDateTimeDeserializer.java` | 需修改：增加字符串格式支持 |
| `yudao-framework/yudao-common/.../TimestampLocalDateTimeSerializer.java` | 参考：已支持 `@JsonFormat` |
| `yudao-framework/.../YudaoJacksonAutoConfiguration.java` | 全局注册点（方案 B 时 INSTANCE 构造需兼容） |
| `yudao-module-infra/.../vo/BloodCollectionFactQueryReqVO.java` | 问题 VO，第 76 行 `@JsonFormat` |
| `yudao-module-infra/.../vo/BloodCollectionFactPageReqVO.java` | 对照参考（GET 接口，`@DateTimeFormat` 正常） |
| `yudao-module-infra/.../BloodCollectionFactController.java` | 聚合接口入口（第 44-49 行） |

---

# Blood Supply Fact - Admin APIs

## 概述

供血事实（`blood_supply_fact`）表接口设计，完全复用采血事实（`blood_collection_fact`）的架构模式：

- Controller → Service → Mapper（`BaseMapperX` + `LambdaQueryWrapperX`）
- 方案 B（`@TenantIgnore`）：DO 不继承 `BaseDO/TenantBaseDO`
- 受限 DSL 聚合查询：白名单枚举 + 强制时间范围 + limit 约束

---

## 表结构与 Model（DO）

表：`blood_supply_fact`

字段映射（Java 类型）：

- `id` → `Long`
- `donation_code` → `String`
- `product_code` → `String`
- `blood_product_name` → `String`
- `abo` → `String`
- `rhd` → `String`
- `blood_amount` → `String`
- `base_unit_value` → `java.math.BigDecimal`
- `blood_expiry_time` → `java.time.LocalDateTime`
- `issue_time` → `java.time.LocalDateTime`
- `issue_type` → `String`
- `return_reason` → `String`
- `issuing_org` → `String`
- `receiving_org` → `String`
- `receiving_org_admin_region` → `String`
- `source_file` → `String`
- `sheet_name` → `String`
- `source_row_num` → `Integer`
- `load_batch_id` → `String`
- `ingested_at` → `java.time.LocalDateTime`

DO 约束：同采血事实，不继承 `BaseDO/TenantBaseDO`，加 `@TenantIgnore`。

---

## 列表/检索查询接口

### 1) 分页列表

`GET /admin-api/infra/blood-supply-fact/page`

权限：`infra:blood-supply-fact:query`

返回：`CommonResult<PageResult<BloodSupplyFactRespVO>>`

分页请求 VO：`BloodSupplyFactPageReqVO extends PageParam`

筛选字段：

- 精确：`donationCode`, `productCode`, `loadBatchId`, `abo`, `rhd`, `bloodProductName`, `issueType`, `issuingOrg`, `receivingOrg`, `receivingOrgAdminRegion`
- 范围：`issueTime[]`, `bloodExpiryTime[]`, `ingestedAt[]`
- 快速检索：`keyword`（仅匹配 `donationCode` / `productCode`）

排序：`issue_time DESC, id DESC`

---

## 聚合查询接口

### 2) 聚合查询

`POST /admin-api/infra/blood-supply-fact/aggregate`

权限：`infra:blood-supply-fact:aggregate`

返回：`CommonResult<BloodSupplyFactAggRespVO>`

### 聚合 DSL（受限白名单）

聚合请求 VO：`BloodSupplyFactAggReqVO`

- `filter: BloodSupplyFactQueryReqVO`（复用分页筛选字段，但不含 pageNo/pageSize）
- `groupBy: List<Integer>`（最多 2 个，使用 `SupplyGroupByFieldEnum` 编码）
- `metrics: List<MetricSpec>`（最多 5 个）
- `orderBy: OrderBySpec`（仅允许按 metrics 别名或 groupBy key 排序）
- `limit: Integer`（默认 100，最大 1000）

强制约束（服务端校验）：

- 若未传 `issueTime` 范围：默认使用最近 3 个月的 `issue_time` 作为查询窗口
- `groupBy.size <= 2`
- `limit <= 1000`
- `metrics` 只能对 `base_unit_value` 做 SUM/AVG，对字符串列只允许 COUNT/COUNT_DISTINCT

### groupBy 白名单（`SupplyGroupByFieldEnum`）

| 编码 | 枚举名 | 别名 | SQL 表达式 |
|------|--------|------|-----------|
| 10 | BLOOD_PRODUCT_NAME | bloodProductName | `blood_product_name` |
| 11 | ABO | abo | `abo` |
| 12 | RHD | rhd | `rhd` |
| 13 | ISSUE_TYPE | issueType | `issue_type` |
| 14 | ISSUING_ORG | issuingOrg | `issuing_org` |
| 15 | RECEIVING_ORG | receivingOrg | `receiving_org` |
| 16 | RECEIVING_ORG_ADMIN_REGION | receivingOrgAdminRegion | `receiving_org_admin_region` |
| 30 | TIME_DAY | timeDay | `DATE_FORMAT(issue_time, '%Y-%m-%d')` |
| 31 | TIME_MONTH | timeMonth | `DATE_FORMAT(issue_time, '%Y-%m')` |

### metrics 白名单（`SupplyMetricFieldEnum`）

| 编码 | 枚举名 | SQL 列 | 允许的操作 |
|------|--------|--------|----------|
| 1 | STAR | `*` | COUNT |
| 2 | DONATION_CODE | `donation_code` | COUNT, COUNT_DISTINCT |
| 3 | PRODUCT_CODE | `product_code` | COUNT, COUNT_DISTINCT |
| 4 | BASE_UNIT_VALUE | `base_unit_value` | COUNT, SUM, AVG |

### 操作编码（复用 `MetricOpEnum`）

| 编码 | 操作 |
|------|------|
| 1 | COUNT |
| 2 | COUNT_DISTINCT |
| 3 | SUM |
| 4 | AVG |

---

## 文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| BloodSupplyFactDO | `dal/dataobject/blood/` | 数据对象，映射 `blood_supply_fact` 表 |
| BloodSupplyFactRespVO | `controller/admin/blood/vo/` | 分页返回 VO |
| BloodSupplyFactPageReqVO | `controller/admin/blood/vo/` | 分页请求 VO（GET，`@DateTimeFormat`）|
| BloodSupplyFactQueryReqVO | `controller/admin/blood/vo/` | 过滤 VO（POST，`@JsonFormat`）|
| BloodSupplyFactAggReqVO | `controller/admin/blood/vo/` | 聚合请求 VO |
| BloodSupplyFactAggRespVO | `controller/admin/blood/vo/` | 聚合返回 VO |
| SupplyGroupByFieldCodeDeserializer | `controller/admin/blood/vo/` | groupBy 编码/别名反序列化 |
| SupplyGroupByFieldEnum | `controller/admin/blood/enums/` | 分组维度白名单枚举 |
| SupplyMetricFieldEnum | `controller/admin/blood/enums/` | 指标字段白名单枚举 |
| MetricOpEnum | `controller/admin/blood/enums/` | 聚合操作枚举（与采血事实共用）|
| BloodSupplyFactMapper | `dal/mysql/blood/` | MyBatis Mapper |
| BloodSupplyFactService | `service/blood/` | 服务接口 |
| BloodSupplyFactServiceImpl | `service/blood/` | 服务实现 |
| BloodSupplyFactController | `controller/admin/blood/` | REST 控制器 |
