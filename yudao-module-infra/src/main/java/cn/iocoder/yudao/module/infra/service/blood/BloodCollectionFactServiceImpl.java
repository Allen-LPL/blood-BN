package cn.iocoder.yudao.module.infra.service.blood;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.GroupByFieldEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.MetricFieldEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.MetricOpEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactQueryReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionFactMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

@Service
@Validated
public class BloodCollectionFactServiceImpl implements BloodCollectionFactService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;
    private static final long MAX_TIME_RANGE_DAYS = 365;
    private static final int DEFAULT_TIME_RANGE_MONTHS = 3;

    @Resource
    private BloodCollectionFactMapper bloodCollectionFactMapper;

    @Override
    public PageResult<BloodCollectionFactDO> getBloodCollectionFactPage(BloodCollectionFactPageReqVO pageReqVO) {
        return bloodCollectionFactMapper.selectPage(pageReqVO);
    }

    @Override
    public BloodCollectionFactAggRespVO aggregate(BloodCollectionFactAggReqVO reqVO) {
        if (reqVO == null) {
            throw invalidParamException("聚合请求不能为空");
        }
        if (CollUtil.isEmpty(reqVO.getMetrics())) {
            throw invalidParamException("metrics 不能为空");
        }

        BloodCollectionFactQueryReqVO filter = normalizeAndValidateFilter(reqVO.getFilter());

        Integer limit = reqVO.getLimit();
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            throw invalidParamException("limit 不能超过 {}", MAX_LIMIT);
        }

        QueryWrapperX<BloodCollectionFactDO> query = buildFilterQuery(filter);

        List<String> selectColumns = new ArrayList<>();
        List<String> groupByColumns = new ArrayList<>();
        List<String> groupByAliases = new ArrayList<>();
        Set<String> orderByAllowlist = new LinkedHashSet<>();

        if (CollUtil.isNotEmpty(reqVO.getGroupBy())) {
            if (reqVO.getGroupBy().size() > 2) {
                throw invalidParamException("groupBy 最多支持 2 个");
            }
            for (Integer groupCode : reqVO.getGroupBy()) {
                GroupByFieldEnum groupEnum = GroupByFieldEnum.valueOfCode(groupCode);
                if (groupEnum == null) {
                    throw invalidParamException("不支持的 groupBy: {}", groupCode);
                }
                String alias = groupEnum.getAlias();
                selectColumns.add(groupEnum.getExpression() + " AS " + alias);
                groupByColumns.add(groupEnum.getExpression());
                groupByAliases.add(alias);
                orderByAllowlist.add(alias);
            }
        }

        List<BloodCollectionFactAggRespVO.MetricMeta> metricMetas = new ArrayList<>();
        List<String> metricAliases = new ArrayList<>();
        for (int i = 0; i < reqVO.getMetrics().size(); i++) {
            BloodCollectionFactAggReqVO.MetricSpec metric = reqVO.getMetrics().get(i);
            if (metric == null) {
                throw invalidParamException("metrics[{}] 不能为空", i);
            }
            MetricOpEnum opEnum = MetricOpEnum.valueOfCode(metric.getOp());
            MetricFieldEnum fieldEnum = MetricFieldEnum.valueOfCode(metric.getField());
            if (metric.getOp() == null || metric.getField() == null) {
                throw invalidParamException("metrics[{}].op 或 metrics[{}].field 不能为空", i, i);
            }
            if (opEnum == null || fieldEnum == null) {
                throw invalidParamException("不支持的 metrics 配置：metrics[{}].op={}, field={}", i, metric.getOp(),
                        metric.getField());
            }
            validateMetric(opEnum, fieldEnum);

            String alias = metric.getAlias();
            if (alias == null || alias.trim().isEmpty()) {
                alias = "metric" + (i + 1);
            }

            String expr = buildMetricExpression(opEnum, fieldEnum);
            selectColumns.add(expr + " AS " + alias);
            metricAliases.add(alias);
            orderByAllowlist.add(alias);

            BloodCollectionFactAggRespVO.MetricMeta meta = new BloodCollectionFactAggRespVO.MetricMeta();
            meta.setAlias(alias);
            meta.setOp(metric.getOp());
            meta.setField(metric.getField());
            metricMetas.add(meta);
        }

        query.select(selectColumns.toArray(new String[0]));
        if (CollUtil.isNotEmpty(groupByColumns)) {
            query.groupBy(groupByColumns);
        }

        if (reqVO.getOrderBy() != null && reqVO.getOrderBy().getKey() != null
                && !reqVO.getOrderBy().getKey().trim().isEmpty()) {
            String orderKey = reqVO.getOrderBy().getKey();
            if (!orderByAllowlist.contains(orderKey)) {
                throw invalidParamException("orderBy.key 不在允许范围内: {}", orderKey);
            }
            boolean isAsc = "ASC".equals(reqVO.getOrderBy().getDirection());
            query.orderBy(true, isAsc, orderKey);
        }
        query.last("LIMIT " + limit);

        List<Map<String, Object>> aggregateRows = bloodCollectionFactMapper.selectMaps(query);

        BloodCollectionFactAggRespVO respVO = new BloodCollectionFactAggRespVO();
        respVO.setGroupBy(groupByAliases);
        respVO.setMetrics(metricMetas);

        List<BloodCollectionFactAggRespVO.Row> rows = new ArrayList<>();
        for (Map<String, Object> aggregateRow : aggregateRows) {
            BloodCollectionFactAggRespVO.Row row = new BloodCollectionFactAggRespVO.Row();
            Map<String, Object> keys = new LinkedHashMap<>();
            for (String alias : groupByAliases) {
                keys.put(alias, aggregateRow.get(alias));
            }
            row.setKeys(keys);

            Map<String, Object> values = new LinkedHashMap<>();
            for (String alias : metricAliases) {
                values.put(alias, aggregateRow.get(alias));
            }
            row.setValues(values);
            rows.add(row);
        }
        respVO.setRows(rows);
        return respVO;
    }

    private QueryWrapperX<BloodCollectionFactDO> buildFilterQuery(BloodCollectionFactQueryReqVO filter) {
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        if (filter == null) {
            return query;
        }
        query.eqIfPresent("archive_id", filter.getArchiveId())
                .eqIfPresent("donation_code", filter.getDonationCode())
                .eqIfPresent("load_batch_id", filter.getLoadBatchId())
                .eqIfPresent("collection_department", filter.getCollectionDepartment())
                .eqIfPresent("collection_site", filter.getCollectionSite())
                .eqIfPresent("organization_mode", filter.getOrganizationMode())
                .eqIfPresent("donation_type", filter.getDonationType())
                .eqIfPresent("gender", filter.getGender())
                .eqIfPresent("full_volume_flag", filter.getFullVolumeFlag())
                .eqIfPresent("archive_blood_type", filter.getArchiveBloodType())
                .eqIfPresent("precheck_blood_type", filter.getPrecheckBloodType())
                .eqIfPresent("unit_admin_region", filter.getUnitAdminRegion())
                .likeIfPresent("org_unit_name", filter.getOrgUnitName())
                .likeIfPresent("system_name", filter.getSystemName())
                .likeIfPresent("parent_unit", filter.getParentUnit())
                .betweenIfPresent("registration_time", filter.getRegistrationTime())
                .betweenIfPresent("precheck_time", filter.getPrecheckTime())
                .betweenIfPresent("blood_collection_time", filter.getBloodCollectionTime())
                .betweenIfPresent("ingested_at", filter.getIngestedAt())
                .geIfPresent("age", filter.getAgeMin())
                .leIfPresent("age", filter.getAgeMax())
                .and(filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty(), w -> w
                        .eq("donation_code", filter.getKeyword())
                        .or()
                        .eq("archive_id", filter.getKeyword()));
        return query;
    }

    private BloodCollectionFactQueryReqVO normalizeAndValidateFilter(BloodCollectionFactQueryReqVO filter) {
        if (filter == null) {
            filter = new BloodCollectionFactQueryReqVO();
        }
        LocalDateTime[] registrationTime = filter.getRegistrationTime();
        LocalDateTime[] precheckTime = filter.getPrecheckTime();
        LocalDateTime[] bloodCollectionTime = filter.getBloodCollectionTime();

        boolean hasRegistrationTime = isValidTimeRange(registrationTime);
        boolean hasPrecheckTime = isValidTimeRange(precheckTime);
        boolean hasBloodCollectionTime = isValidTimeRange(bloodCollectionTime);
        if (!hasBloodCollectionTime) {
            LocalDateTime now = LocalDateTime.now();
            filter.setBloodCollectionTime(new LocalDateTime[] { now.minusMonths(DEFAULT_TIME_RANGE_MONTHS), now });
            return filter;
        } else {
            filter.setBloodCollectionTime(bloodCollectionTime);
        }
        if (hasRegistrationTime) {
            validateTimeSpan(registrationTime);
        }
        if (hasPrecheckTime) {
            validateTimeSpan(precheckTime);
        }
        if (hasBloodCollectionTime) {
            validateTimeSpan(bloodCollectionTime);
        }
        return filter;
    }

    private boolean isValidTimeRange(LocalDateTime[] times) {
        return times != null && times.length >= 2 && times[0] != null && times[1] != null;
    }

    private void validateTimeSpan(LocalDateTime[] times) {
        long days = Duration.between(times[0], times[1]).toDays();
        if (days > MAX_TIME_RANGE_DAYS) {
            throw invalidParamException("时间跨度不能超过 {} 天", MAX_TIME_RANGE_DAYS);
        }
    }

    private void validateMetric(MetricOpEnum opEnum, MetricFieldEnum fieldEnum) {
        if ((opEnum == MetricOpEnum.SUM || opEnum == MetricOpEnum.AVG)
                && fieldEnum != MetricFieldEnum.BASE_UNIT_VALUE
                && fieldEnum != MetricFieldEnum.AGE) {
            throw invalidParamException("SUM/AVG 仅支持 base_unit_value 或 age 字段");
        }
        if (opEnum == MetricOpEnum.COUNT_DISTINCT && fieldEnum == MetricFieldEnum.STAR) {
            throw invalidParamException("COUNT_DISTINCT 不支持 STAR 字段");
        }
    }

    private String buildMetricExpression(MetricOpEnum opEnum, MetricFieldEnum fieldEnum) {
        switch (opEnum) {
            case COUNT:
                return "COUNT(" + fieldEnum.getColumn() + ")";
            case COUNT_DISTINCT:
                return "COUNT(DISTINCT " + fieldEnum.getColumn() + ")";
            case SUM:
                return "SUM(" + fieldEnum.getColumn() + ")";
            case AVG:
                return "AVG(" + fieldEnum.getColumn() + ")";
            default:
                throw invalidParamException("不支持的聚合操作");
        }
    }

}
