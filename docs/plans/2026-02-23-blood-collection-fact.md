# Blood Collection Fact Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 
在 `yudao-module-infra` 中完整接入 `blood_collection_fact`，实现：列表分页查询接口、检索查询（基于筛选条件）、聚合查询接口，并补齐必要的 Model/VO/Service/Mapper。

**Architecture:**
遵循现有 yudao 分层：`controller/admin` → `service` → `dal/mysql` → `dal/dataobject`。分页查询复用 `BaseMapperX` + `LambdaQueryWrapperX`；聚合查询用 `QueryWrapperX` + `selectMaps`，并通过 enum 白名单控制 groupBy/metrics。

**Tech Stack:**
Spring Boot 2.7, MyBatis-Plus, yudao framework `CommonResult`/`PageResult`/`PageParam`, JUnit 5（通过 `spring-boot-starter-test`）。

---

### Task 1: Add package skeleton and API permission keys

**Files:**
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/BloodCollectionFactController.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactService.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactServiceImpl.java`

**Step 1: Write the failing test**

Create a minimal controller/service test that boots Spring and verifies the bean wiring exists.

`yudao-module-infra/src/test/java/cn/iocoder/yudao/module/infra/blood/BloodCollectionFactWiringTest.java`

```java
@SpringBootTest
public class BloodCollectionFactWiringTest {

    @Resource
    private BloodCollectionFactService service;

    @Test
    public void test_wiring() {
        assertNotNull(service);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactWiringTest test`
Expected: FAIL because test deps or classes not yet present.

**Step 3: Write minimal implementation**

- Add `spring-boot-starter-test` dependency to `yudao-module-infra/pom.xml` (scope test)
- Add minimal `BloodCollectionFactService` + `Impl` annotated with `@Service`

**Step 4: Run test to verify it passes**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactWiringTest test`
Expected: PASS.

---

### Task 2: Add DO + Mapper for blood_collection_fact

**Files:**
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/dal/dataobject/blood/BloodCollectionFactDO.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/dal/mysql/blood/BloodCollectionFactMapper.java`

**Step 1: Write the failing test**

Add a unit test that verifies the mapper bean loads.

`yudao-module-infra/src/test/java/cn/iocoder/yudao/module/infra/blood/BloodCollectionFactMapperWiringTest.java`

```java
@SpringBootTest
public class BloodCollectionFactMapperWiringTest {

    @Resource
    private BloodCollectionFactMapper mapper;

    @Test
    public void test_wiring() {
        assertNotNull(mapper);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactMapperWiringTest test`
Expected: FAIL because mapper/DO not yet created.

**Step 3: Write minimal implementation**

- `BloodCollectionFactDO`:
  - `@TableName("blood_collection_fact")`
  - `@TableId` on `id`
  - `@TenantIgnore` (default decision; see `API.md`)
  - Do NOT extend `BaseDO/TenantBaseDO`
- `BloodCollectionFactMapper extends BaseMapperX<BloodCollectionFactDO>` with `@Mapper`

**Step 4: Run test to verify it passes**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactMapperWiringTest test`
Expected: PASS.

---

### Task 3: Implement paged list (list + search filters)

**Files:**
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/vo/BloodCollectionFactPageReqVO.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/vo/BloodCollectionFactRespVO.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/dal/mysql/blood/BloodCollectionFactMapper.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactService.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactServiceImpl.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/BloodCollectionFactController.java`

**Step 1: Write the failing test**

Add a test that calls the service method and asserts it returns a non-null PageResult (can be empty).

`yudao-module-infra/src/test/java/cn/iocoder/yudao/module/infra/blood/BloodCollectionFactPageContractTest.java`

```java
@SpringBootTest
public class BloodCollectionFactPageContractTest {

    @Resource
    private BloodCollectionFactService service;

    @Test
    public void test_page_returns_pageResult() {
        BloodCollectionFactPageReqVO reqVO = new BloodCollectionFactPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        PageResult<BloodCollectionFactDO> page = service.getBloodCollectionFactPage(reqVO);
        assertNotNull(page);
        assertNotNull(page.getList());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactPageContractTest test`
Expected: FAIL because service API not yet implemented.

**Step 3: Write minimal implementation**

- Mapper: add `default PageResult<BloodCollectionFactDO> selectPage(BloodCollectionFactPageReqVO reqVO)`
  - use `LambdaQueryWrapperX` with `eqIfPresent/likeIfPresent/betweenIfPresent/ge/le`
  - enforce stable sort: `orderByDesc(blood_collection_time)` then `orderByDesc(id)`
- Service: `PageResult<BloodCollectionFactDO> getBloodCollectionFactPage(BloodCollectionFactPageReqVO reqVO)` delegates to mapper
- Controller: `GET /infra/blood-collection-fact/page`
  - `@PreAuthorize("@ss.hasPermission('infra:blood-collection-fact:query')")`
  - return `CommonResult.success(BeanUtils.toBean(pageResult, BloodCollectionFactRespVO.class))`

**Step 4: Run test to verify it passes**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactPageContractTest test`
Expected: PASS.

Note: This test doesn’t verify DB correctness; it is a contract-level guard ensuring wiring + non-null outputs. DB correctness tests can be added later when a test DB strategy is chosen.

---

### Task 4: Implement aggregation endpoint (constrained DSL)

**Files:**
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/vo/agg/BloodCollectionFactAggReqVO.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/vo/agg/BloodCollectionFactAggRespVO.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/enums/GroupByFieldEnum.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/enums/MetricOpEnum.java`
- Create: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/enums/MetricFieldEnum.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactService.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/blood/BloodCollectionFactServiceImpl.java`
- Modify: `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/blood/BloodCollectionFactController.java`

**Step 1: Write the failing test**

Add a test that verifies validation rejects missing time range for aggregation.

`yudao-module-infra/src/test/java/cn/iocoder/yudao/module/infra/blood/BloodCollectionFactAggValidationTest.java`

```java
@SpringBootTest
public class BloodCollectionFactAggValidationTest {

    @Resource
    private BloodCollectionFactService service;

    @Test
    public void test_aggregate_requires_time_range() {
        BloodCollectionFactAggReqVO reqVO = new BloodCollectionFactAggReqVO();
        // no time range
        assertThrows(IllegalArgumentException.class, () -> service.aggregate(reqVO));
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactAggValidationTest test`
Expected: FAIL because aggregate method not implemented.

**Step 3: Write minimal implementation**

- Service `aggregate(reqVO)`:
  - validate time range presence + limit + groupBy size
  - build `QueryWrapperX<BloodCollectionFactDO>`
  - apply filter where clauses
  - apply groupBy + metrics via enum mapping
  - execute `mapper.selectMaps(wrapper)`
  - map to `AggRespVO`
- Controller: `POST /infra/blood-collection-fact/aggregate` with permission `infra:blood-collection-fact:aggregate`

**Step 4: Run test to verify it passes**

Run: `mvn -pl yudao-module-infra -Dtest=BloodCollectionFactAggValidationTest test`
Expected: PASS.

---

### Task 5: Verification

**Step 1: Compile entire repo**

Run: `mvn -B -pl yudao-server -am package -Dmaven.test.skip=true`
Expected: BUILD SUCCESS.

**Step 2: Run all tests (if any)**

Run: `mvn -pl yudao-module-infra test`
Expected: tests pass.

---

### Task 6: Documentation update

**Files:**
- Modify: `API.md`

Ensure the final endpoints/VOs/enums match the implementation.
