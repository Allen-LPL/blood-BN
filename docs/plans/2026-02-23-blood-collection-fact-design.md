# Blood Collection Fact (blood_collection_fact) Design

**Goal:** 
在现有管理后台（`/admin-api`）中接入 MySQL 表 `blood_collection_fact`，提供：列表分页查询、检索查询（基于筛选条件）、聚合查询（统计/分组指标）。

**Architecture:**
遵循仓库既有 yudao 分层：`controller/admin` → `service` → `dal/mysql` → `dal/dataobject`。查询以 MyBatis-Plus 为主，分页使用 `BaseMapperX` + `LambdaQueryWrapperX`；聚合使用 `QueryWrapperX`（字符串列名）但所有可选列均由后端白名单控制。

**Tech Stack:**
Spring Boot 2.7, Spring Security `@PreAuthorize`, MyBatis-Plus, yudao framework `CommonResult`/`PageResult`/`PageParam`。

---

## 1. 约束与默认决策

### 1.1 多租户

本仓库默认启用多租户插件（`yudao.tenant.enable=true`）。

当前提供的 `blood_collection_fact` DDL 不包含 `tenant_id`。如果直接把它当普通 DO 使用，租户拦截器可能会拼接 `tenant_id` 导致 SQL 报错。

默认落地（不阻塞推进）：

- 将该表作为“全局事实表/单租户表”处理：
  - DO 标注 `@TenantIgnore`
  - DO **不继承** `BaseDO`/`TenantBaseDO`

如需多租户隔离：需要补齐 `tenant_id`（以及按需要补 `create_time/update_time/deleted`），并将 DO 改为继承 `TenantBaseDO`，同时移除 `@TenantIgnore`。

### 1.2 查询形态

- “检索查询”不单独做接口：统一沉淀到分页接口的筛选字段中。
- 聚合查询采用“受限 DSL”：前端可自由选择维度/指标，但必须在后端白名单内，避免 SQL 注入与不可控性能。

---

## 2. 模块与包路径

由于当前启用的模块仅有 `yudao-module-system`、`yudao-module-infra`（`yudao-module-report` 未启用），为了最小改动与可立即落地，先落在 infra 模块：

- Module: `yudao-module-infra`
- Base package: `cn.iocoder.yudao.module.infra`
- 新增功能包：
  - `cn.iocoder.yudao.module.infra.controller.admin.blood`
  - `cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*`
  - `cn.iocoder.yudao.module.infra.service.blood`
  - `cn.iocoder.yudao.module.infra.dal.mysql.blood`
  - `cn.iocoder.yudao.module.infra.dal.dataobject.blood`

---

## 3. API 设计

路径前缀由 `yudao.web.admin-api.prefix=/admin-api` 统一配置，Controller 仅写模块内部 `@RequestMapping`。

### 3.1 列表分页

- Method: `GET`
- Path: `/admin-api/infra/blood-collection-fact/page`
- Permission: `infra:blood-collection-fact:query`
- Response: `CommonResult<PageResult<BloodCollectionFactRespVO>>`

Query（建议字段，详见 `API.md`）：

- 标识：`archiveId`, `donationCode`, `loadBatchId`
- 维度：`collectionDepartment`, `collectionSite`, `organizationMode`, `donationType`, `gender`, `fullVolumeFlag`, `archiveBloodType`, `precheckBloodType`, `unitAdminRegion`
- 时间范围：`registrationTime[]`, `precheckTime[]`, `bloodCollectionTime[]`, `ingestedAt[]`
- 年龄范围：`ageMin`, `ageMax`

排序：默认 `bloodCollectionTime DESC, id DESC`。

### 3.2 聚合查询

- Method: `POST`
- Path: `/admin-api/infra/blood-collection-fact/aggregate`
- Permission: `infra:blood-collection-fact:aggregate`
- Request body: `BloodCollectionFactAggReqVO`
- Response: `CommonResult<BloodCollectionFactAggRespVO>`

聚合 DSL（强约束）：

- `filter`: 复用分页的筛选字段（不含分页参数）
- `groupBy`: `List<GroupByFieldEnum>`，最多 2 个
- `metrics`: `List<MetricSpec>`，最多 5 个
- `orderBy`: 仅允许按 groupBy key 或 metric alias 排序
- `limit`: 默认 100，最大 1000

强制要求：必须提供至少一个时间范围（建议优先 `bloodCollectionTime[]`），且限制最大跨度（例如 180 天），避免全表扫描。

可选增强（后续）：`GET /admin-api/infra/blood-collection-fact/aggregate/meta` 返回白名单维度/指标给前端配置下拉项。

---

## 4. 数据模型

### 4.1 DO

`BloodCollectionFactDO` 字段与表一一映射（text 用 String）。

关键约束：

- `@TableName("blood_collection_fact")`
- `@TableId`（MySQL 自增）
- `@TenantIgnore`
- 不继承 `BaseDO`/`TenantBaseDO`

### 4.2 VO

- `BloodCollectionFactPageReqVO extends PageParam`
- `BloodCollectionFactRespVO`（列表视图建议不返回大 text 字段；如需详情再补 `/get`）
- `BloodCollectionFactAggReqVO`
- `BloodCollectionFactAggRespVO`

---

## 5. Mapper/Service 设计

### 5.1 分页 Mapper

按仓库惯例，在 `*Mapper` 中提供 `default PageResult<DO> selectPage(ReqVO reqVO)`：

- 使用 `LambdaQueryWrapperX` 拼接 `eqIfPresent/likeIfPresent/betweenIfPresent/ge/le` 等
- `selectPage(reqVO, query)` 返回分页

### 5.2 聚合查询

在 Service 构建 `QueryWrapperX<DO>`：

- 使用同样的 filter 规则拼接 where
- 根据 `groupBy` 白名单拼接 `select(...)` 与 `groupBy(...)`
- 根据 metrics 白名单拼接聚合表达式（例如 `COUNT(1) AS cnt`, `SUM(base_unit_value) AS sumBaseUnitValue`）
- `mapper.selectMaps(wrapper)` 返回 `List<Map<String,Object>>`，再映射为 `AggRespVO`。

所有可拼接的列名、表达式均来自 enum 映射，严禁从客户端透传原始列名。

---

## 6. 校验/安全

- `@Valid` 校验 VO
- groupBy/metric op/metric field 均使用 `@InEnum`（框架已有）
- 聚合必须带时间范围 + limit 上限 + groupBy 数量上限
- orderBy 仅允许白名单字段（group key 或 metric alias）

---

## 7. 性能

当前 DDL 已有索引：`archive_id`、`donation_code`、`precheck_time`、`load_batch_id`。

聚合/筛选会频繁用到的联合索引，需要按“实际 Top N 场景”补充（例如 `precheck_time + collection_site`）。

聚合时间桶如果是核心需求：建议增加派生列（date/month）而不是在 SQL 里 `DATE_FORMAT(...)`。

---

## 8. 测试策略

本仓库当前基本无 `src/test/java`，因此需要先补齐 infra 模块的测试依赖（`spring-boot-starter-test`，test scope），再做最小可运行测试：

- 校验测试：非法 groupBy/metric/limit/time span 被拒绝
- Mapper/Service 逻辑测试：对 wrapper 拼接进行断言（或在可用数据库环境下做集成测试）

---

## 9. 关联文档

- `API.md`：对外 API 结论与约束汇总
