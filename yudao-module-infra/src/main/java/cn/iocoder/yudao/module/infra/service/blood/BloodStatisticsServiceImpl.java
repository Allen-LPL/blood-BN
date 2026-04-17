package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionMainSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionFactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionMainSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodSupplyFactMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class BloodStatisticsServiceImpl implements BloodStatisticsService {

    private static final int DEFAULT_TIME_RANGE_MONTHS = 3;

    /** blood_volume（varchar）转为数值后，< 50 视为单采血小板，>= 50 视为全血 */
    private static final String WHOLE_BLOOD_CONDITION = "CAST(blood_volume AS DECIMAL(10,2)) >= 50";
    private static final String PLATELET_CONDITION = "CAST(blood_volume AS DECIMAL(10,2)) < 50";

    private static final String TREND_TYPE_VOLUNTARY = "自愿献血";
    private static final String TREND_TYPE_GROUP = "团体无偿";
    private static final String TREND_TYPE_WHOLE_BLOOD = "全血采血量";
    private static final String TREND_TYPE_PLATELET = "血小板单采量";

    private static final String ORG_MODE_PERSONAL = "个人无偿";
    private static final String ORG_MODE_HOSPITAL_GROUP = "医院团体";
    private static final String ORG_MODE_VOLUNTARY_CONDITION = "organization_mode = '自愿献血'";
    private static final String ORG_MODE_GROUP_CONDITION = "organization_mode = '团体无偿'";

    private static final List<String> STANDARD_BLOOD_TYPES = Arrays.asList("A", "B", "O", "AB");

    @Resource
    private BloodCollectionFactMapper collectionMapper;

    @Resource
    private BloodCollectionMainSiteMapper mainSiteMapper;

    @Resource
    private BloodSupplyFactMapper supplyMapper;

    // ==================== 1. 献血者分布 ====================

    @Override
    public DonorDistributionRespVO getDonorDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "COUNT(*) AS totalCount",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION
                        + " THEN 1 ELSE 0 END) AS wholeBloodCount",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION
                        + " THEN 1 ELSE 0 END) AS plateletCount",
                "SUM(CASE WHEN gender = '男' THEN 1 ELSE 0 END) AS maleCount",
                "SUM(CASE WHEN gender = '女' THEN 1 ELSE 0 END) AS femaleCount");
        query.between("blood_collection_time", range[0], range[1]);

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);
        Map<String, Object> row = rows.get(0);

        long totalCount = toLong(row.get("totalCount"));
        long wholeBloodCount = toLong(row.get("wholeBloodCount"));
        long plateletCount = toLong(row.get("plateletCount"));
        long maleCount = toLong(row.get("maleCount"));
        long femaleCount = toLong(row.get("femaleCount"));

        DonorDistributionRespVO resp = new DonorDistributionRespVO();
        resp.setTotalCount(totalCount);
        resp.setWholeBloodCount(wholeBloodCount);
        resp.setPlateletCount(plateletCount);
        resp.setMaleCount(maleCount);
        resp.setFemaleCount(femaleCount);
        resp.setWholeBloodRatio(calcRatio(wholeBloodCount, totalCount));
        resp.setPlateletRatio(calcRatio(plateletCount, totalCount));
        resp.setMaleRatio(calcRatio(maleCount, totalCount));
        resp.setFemaleRatio(calcRatio(femaleCount, totalCount));
        resp.setAgeDistribution(queryAgeDistribution(range, totalCount));
        return resp;
    }

    // ==================== 2. 全血采血量趋势 ====================

    @Override
    public CollectionTrendRespVO getWholeBloodTrend(LocalDateTime startTime, LocalDateTime endTime, String period,
            String collectionDepartment, List<String> trendTypes) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);
        String dateFormat = getDateFormatExpression(period);
        List<String> normalizedTrendTypes = normalizeTrendTypes(trendTypes);

        List<String> bloodVolumeConditions = buildBloodVolumeConditions(normalizedTrendTypes);
        List<String> organizationModes = buildOrganizationModesByTrendTypes(normalizedTrendTypes);

        CollectionTrendRespVO resp = new CollectionTrendRespVO();
        resp.setItems(queryCollectionTrendItems(range, dateFormat, collectionDepartment,
                bloodVolumeConditions, organizationModes));
        return resp;
    }

    // ==================== 3. 单采血小板采血量趋势 ====================

    @Override
    public CollectionTrendRespVO getPlateletTrend(LocalDateTime startTime, LocalDateTime endTime, String period) {
        return getWholeBloodTrend(startTime, endTime, period,
                null, Collections.singletonList(TREND_TYPE_PLATELET));
    }

    // ==================== 4. 献血区域类型采血量 ====================

    @Override
    public DistrictCollectionRespVO getDistrictCollection(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 按 collection_site 聚合
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_site AS collectionSite",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_site");
        query.groupBy("collection_site");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        // 按区域类型汇总
        Map<String, BigDecimal> areaUnit = new LinkedHashMap<>();
        Map<String, Long> areaCount = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String site = (String) row.get("collectionSite");
            String areaType = classifySiteToAreaType(site);
            BigDecimal unit = toBigDecimal(row.get("totalUnit"));
            long cnt = toLong(row.get("cnt"));
            areaUnit.merge(areaType, unit, BigDecimal::add);
            areaCount.merge(areaType, cnt, Long::sum);
        }

        List<DistrictCollectionRespVO.Item> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : areaUnit.entrySet()) {
            DistrictCollectionRespVO.Item item = new DistrictCollectionRespVO.Item();
            item.setAreaType(entry.getKey());
            item.setTotalUnit(entry.getValue());
            item.setCount(areaCount.get(entry.getKey()));
            items.add(item);
        }
        items.sort((a, b) -> {
            int unitCompare = b.getTotalUnit().compareTo(a.getTotalUnit());
            if (unitCompare != 0) {
                return unitCompare;
            }
            int countCompare = Long.compare(b.getCount(), a.getCount());
            if (countCompare != 0) {
                return countCompare;
            }
            return a.getAreaType().compareTo(b.getAreaType());
        });

        DistrictCollectionRespVO resp = new DistrictCollectionRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 5. 各血液中心采血排行 ====================

    @Override
    public CenterRankingRespVO getCenterRanking(LocalDateTime startTime, LocalDateTime endTime,
            List<String> trendTypes, List<String> orgModes) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 1. 按 trendTypes 聚合
        List<CenterRankingRespVO.Item> trendRanking = queryCenterRankingByTrendTypes(range, trendTypes);

        // 2. 按 orgModes 聚合
        List<CenterRankingRespVO.Item> orgModeRanking = queryCenterRankingByOrgModes(range, orgModes);

        CenterRankingRespVO resp = new CenterRankingRespVO();
        resp.setTrendRanking(trendRanking);
        resp.setOrgModeRanking(orgModeRanking);
        return resp;
    }

    @Override
    public DonationTypeRankingRespVO getDonationTypeRanking(LocalDateTime startTime, LocalDateTime endTime,
            List<String> bloodTypes) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);
        List<String> bloodTypeConditions = buildBloodTypeConditions(bloodTypes);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_site AS collectionSite",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_site");
        if (!bloodTypeConditions.isEmpty()) {
            query.and(w -> {
                for (int i = 0; i < bloodTypeConditions.size(); i++) {
                    if (i == 0) {
                        w.apply(bloodTypeConditions.get(i));
                    } else {
                        w.or().apply(bloodTypeConditions.get(i));
                    }
                }
            });
        }
        query.groupBy("collection_site");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        Map<String, BigDecimal> totalUnitMap = new LinkedHashMap<>();
        Map<String, Long> countMap = new LinkedHashMap<>();
        totalUnitMap.put("献血方舱", BigDecimal.ZERO);
        totalUnitMap.put("献血屋", BigDecimal.ZERO);
        totalUnitMap.put("献血车", BigDecimal.ZERO);
        countMap.put("献血方舱", 0L);
        countMap.put("献血屋", 0L);
        countMap.put("献血车", 0L);

        for (Map<String, Object> row : rows) {
            String siteType = classifyDonationSiteType((String) row.get("collectionSite"));
            if (siteType == null) {
                continue;
            }
            totalUnitMap.put(siteType, totalUnitMap.get(siteType).add(toBigDecimal(row.get("totalUnit"))));
            countMap.put(siteType, countMap.get(siteType) + toLong(row.get("cnt")));
        }

        List<DonationTypeRankingRespVO.Item> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : totalUnitMap.entrySet()) {
            DonationTypeRankingRespVO.Item item = new DonationTypeRankingRespVO.Item();
            item.setTypeName(entry.getKey());
            item.setTotalUnit(entry.getValue());
            item.setCount(countMap.get(entry.getKey()));
            items.add(item);
        }
        items.sort((a, b) -> {
            int unitCompare = b.getTotalUnit().compareTo(a.getTotalUnit());
            if (unitCompare != 0) {
                return unitCompare;
            }
            int countCompare = Long.compare(b.getCount(), a.getCount());
            if (countCompare != 0) {
                return countCompare;
            }
            return a.getTypeName().compareTo(b.getTypeName());
        });
        int rank = 1;
        for (DonationTypeRankingRespVO.Item item : items) {
            item.setRank(rank++);
        }

        DonationTypeRankingRespVO resp = new DonationTypeRankingRespVO();
        resp.setItems(items);
        return resp;
    }

    @Override
    public CollectionSiteRankingRespVO getCollectionSiteRanking(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);
        List<Map<String, Object>> rows = collectionMapper.selectCollectionSiteRanking(range[0], range[1]);
        List<CollectionSiteRankingRespVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            CollectionSiteRankingRespVO.Item item = new CollectionSiteRankingRespVO.Item();
            item.setRank(rank++);
            item.setCollectionAgency((String) row.get("collectionAgency"));
            item.setOperatingOrg((String) row.get("operatingOrg"));
            item.setSiteNameSystem((String) row.get("siteNameSystem"));
            item.setTotalUnit(toBigDecimal(row.get("totalUnit")));
            item.setWholeBloodUnit(toBigDecimal(row.get("wholeBloodUnit")));
            item.setPlateletUnit(toBigDecimal(row.get("plateletUnit")));
            item.setCount(toLong(row.get("cnt")));
            items.add(item);
        }

        CollectionSiteRankingRespVO resp = new CollectionSiteRankingRespVO();
        resp.setItems(items);
        return resp;
    }

    private List<CenterRankingRespVO.Item> queryCenterRankingByTrendTypes(LocalDateTime[] range,
            List<String> trendTypes) {
        // 1) 基础聚合：按采血机构分组，统计总采血量与采血人次
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_department AS centerName",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_department");
        query.groupBy("collection_department");
        if (trendTypes != null && !trendTypes.isEmpty()) {
            // 2) 将前端多选的趋势类型转换为 SQL 条件，并使用 OR 聚合
            List<String> orConditions = new ArrayList<>();
            for (String trendType : trendTypes) {
                orConditions.addAll(buildConditionsByTrendType(trendType));
            }
            query.and(w -> {
                for (int i = 0; i < orConditions.size(); i++) {
                    if (i == 0)
                        w.apply(orConditions.get(i));
                    else
                        w.or().apply(orConditions.get(i));
                }
            });
        }
        // 3) 按采血量降序，生成机构排行
        query.orderByDesc("totalUnit");
        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);
        List<CenterRankingRespVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            CenterRankingRespVO.Item item = new CenterRankingRespVO.Item();
            item.setRank(rank++);
            item.setCenterName((String) row.get("centerName"));
            item.setTotalUnit(toBigDecimal(row.get("totalUnit")));
            item.setCount(toLong(row.get("cnt")));
            items.add(item);
        }
        return items;
    }

    private List<CenterRankingRespVO.Item> queryCenterRankingByOrgModes(LocalDateTime[] range, List<String> orgModes) {
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_department AS centerName",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_department");
        query.groupBy("collection_department");
        if (orgModes != null && !orgModes.isEmpty()) {
            query.and(w -> {
                for (int i = 0; i < orgModes.size(); i++) {
                    if (i == 0)
                        w.apply("organization_mode = '" + orgModes.get(i) + "'");
                    else
                        w.or().apply("organization_mode = '" + orgModes.get(i) + "'");
                }
            });
        }
        query.orderByDesc("totalUnit");
        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);
        List<CenterRankingRespVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            CenterRankingRespVO.Item item = new CenterRankingRespVO.Item();
            item.setRank(rank++);
            item.setCenterName((String) row.get("centerName"));
            item.setTotalUnit(toBigDecimal(row.get("totalUnit")));
            item.setCount(toLong(row.get("cnt")));
            items.add(item);
        }
        return items;
    }

    // ==================== 6. 各区采血量统计 ====================

    @Override
    public QuarterlyCollectionRespVO getQuarterlySummary(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_department AS collectionDepartment",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS wholeBloodUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS plateletUnit",
                "COUNT(*) AS totalCount");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_department");
        query.groupBy("collection_department");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        List<QuarterlyCollectionRespVO.Item> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            QuarterlyCollectionRespVO.Item item = new QuarterlyCollectionRespVO.Item();
            item.setDistrict(mapDepartmentToDistrict((String) row.get("collectionDepartment")));
            item.setWholeBloodUnit(toBigDecimal(row.get("wholeBloodUnit")));
            item.setPlateletUnit(toBigDecimal(row.get("plateletUnit")));
            item.setTotalUnit(item.getWholeBloodUnit().add(item.getPlateletUnit()));
            item.setTotalCount(toLong(row.get("totalCount")));
            items.add(item);
        }
        items.sort((a, b) -> b.getTotalUnit().compareTo(a.getTotalUnit()));

        QuarterlyCollectionRespVO resp = new QuarterlyCollectionRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 7. 首页统计概览 ====================

    @Override
    public HomePageStatsRespVO getHomePageStats(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 1) 采血统计
        QueryWrapperX<BloodCollectionFactDO> collectionQuery = new QueryWrapperX<>();
        collectionQuery.select(
                "COUNT(*) AS totalCount",
                "SUM(base_unit_value) AS totalUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS wholeBloodUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS plateletUnit",
                "SUM(CASE WHEN gender = '男' THEN 1 ELSE 0 END) AS maleCount",
                "SUM(CASE WHEN gender = '女' THEN 1 ELSE 0 END) AS femaleCount");
        collectionQuery.between("blood_collection_time", range[0], range[1]);
        List<Map<String, Object>> collectionRows = collectionMapper.selectMaps(collectionQuery);
        Map<String, Object> cRow = collectionRows.get(0);

        // 2) 供血统计
        QueryWrapperX<BloodSupplyFactDO> supplyQuery = new QueryWrapperX<>();
        supplyQuery.select(
                "COUNT(*) AS totalCount",
                "SUM(base_unit_value) AS totalUnit");
        supplyQuery.between("issue_time", range[0], range[1]);
        List<Map<String, Object>> supplyRows = supplyMapper.selectMaps(supplyQuery);
        Map<String, Object> sRow = supplyRows.get(0);

        // 3) 运行中的血液中心数量
        long activeSiteCount = mainSiteMapper.selectCount(
                new LambdaQueryWrapper<BloodCollectionMainSiteDO>()
                        .eq(BloodCollectionMainSiteDO::getState, 1));

        // 4) 构建响应
        HomePageStatsRespVO resp = new HomePageStatsRespVO();
        resp.setCollectionTotalCount(toLong(cRow.get("totalCount")));
        resp.setCollectionTotalUnit(toBigDecimal(cRow.get("totalUnit")));
        resp.setWholeBloodUnit(toBigDecimal(cRow.get("wholeBloodUnit")));
        resp.setPlateletUnit(toBigDecimal(cRow.get("plateletUnit")));
        resp.setMaleCount(toLong(cRow.get("maleCount")));
        resp.setFemaleCount(toLong(cRow.get("femaleCount")));
        resp.setSupplyTotalCount(toLong(sRow.get("totalCount")));
        resp.setSupplyTotalUnit(toBigDecimal(sRow.get("totalUnit")));
        resp.setActiveSiteCount(activeSiteCount);
        return resp;
    }

    // ==================== 8. 北京血液中心信息及统计 ====================

    @Override
    public MainSiteStatsRespVO getMainSiteStats(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 1) 查询所有血液中心
        List<BloodCollectionMainSiteDO> mainSites = mainSiteMapper.selectList(
                new LambdaQueryWrapper<BloodCollectionMainSiteDO>()
                        .eq(BloodCollectionMainSiteDO::getState, 1));

        // 2) 按 issuing_org 聚合供血量
        QueryWrapperX<BloodSupplyFactDO> supplyQuery = new QueryWrapperX<>();
        supplyQuery.select(
                "issuing_org AS issuingOrg",
                "SUM(base_unit_value) AS totalUnit");
        supplyQuery.between("issue_time", range[0], range[1]);
        supplyQuery.isNotNull("issuing_org");
        supplyQuery.groupBy("issuing_org");
        List<Map<String, Object>> supplyRows = supplyMapper.selectMaps(supplyQuery);
        Map<String, BigDecimal> supplyMap = new HashMap<>();
        for (Map<String, Object> row : supplyRows) {
            supplyMap.put((String) row.get("issuingOrg"), toBigDecimal(row.get("totalUnit")));
        }

        // 3) 按 collection_department 聚合采血量
        QueryWrapperX<BloodCollectionFactDO> collectionQuery = new QueryWrapperX<>();
        collectionQuery.select(
                "collection_department AS collectionDepartment",
                "SUM(base_unit_value) AS totalUnit");
        collectionQuery.between("blood_collection_time", range[0], range[1]);
        collectionQuery.isNotNull("collection_department");
        collectionQuery.groupBy("collection_department");
        List<Map<String, Object>> collectionRows = collectionMapper.selectMaps(collectionQuery);
        Map<String, BigDecimal> collectionMap = new HashMap<>();
        for (Map<String, Object> row : collectionRows) {
            collectionMap.put((String) row.get("collectionDepartment"), toBigDecimal(row.get("totalUnit")));
        }

        // 4) 组装响应：每个血液中心 + 坐标 + 供血统计 + 采血统计
        List<MainSiteStatsRespVO.Item> items = new ArrayList<>();
        for (BloodCollectionMainSiteDO site : mainSites) {
            MainSiteStatsRespVO.Item item = new MainSiteStatsRespVO.Item();
            item.setId(site.getId());
            item.setName(site.getCollectionOperatingOrg());
            item.setLevel(site.getType() == 1 ? "一级" : "二级");
            item.setState(site.getState());
            item.setLng(site.getCoordinateLng());
            item.setLat(site.getCoordinateLat());
            item.setSupplyTotalUnit(supplyMap.getOrDefault(site.getCollectionOperatingOrg(), BigDecimal.ZERO));
            item.setCollectionTotalUnit(collectionMap.getOrDefault(site.getCollectionOperatingOrg(), BigDecimal.ZERO));
            item.setValue(collectionMap.getOrDefault(site.getCollectionOperatingOrg(), BigDecimal.ZERO));
            items.add(item);
        }

        MainSiteStatsRespVO resp = new MainSiteStatsRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 9. 采血表血型占比统计 ====================

    @Override
    public BloodTypeDistributionRespVO getBloodTypeDistribution(LocalDateTime startTime, LocalDateTime endTime,
            List<String> bloodTypes) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        List<String> bloodTypeConditions = buildBloodTypeConditions(bloodTypes);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "precheck_blood_type AS bloodType",
                "COUNT(*) AS cnt",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION
                        + " THEN 1 ELSE 0 END) AS wholeBloodCount",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION
                        + " THEN 1 ELSE 0 END) AS plateletCount",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS wholeBloodUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION
                        + " THEN base_unit_value ELSE 0 END) AS plateletUnit");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("precheck_blood_type");
        query.in("precheck_blood_type", STANDARD_BLOOD_TYPES);
        if (!bloodTypeConditions.isEmpty()) {
            query.and(w -> {
                for (int i = 0; i < bloodTypeConditions.size(); i++) {
                    if (i == 0) {
                        w.apply(bloodTypeConditions.get(i));
                    } else {
                        w.or().apply(bloodTypeConditions.get(i));
                    }
                }
            });
        }
        query.groupBy("precheck_blood_type");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        Map<String, Map<String, Object>> rowMap = new HashMap<>();
        long totalCount = 0L;
        long wholeBloodTotalCount = 0L;
        long plateletTotalCount = 0L;
        BigDecimal wholeBloodTotalUnit = BigDecimal.ZERO;
        BigDecimal plateletTotalUnit = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            String bloodType = (String) row.get("bloodType");
            if (bloodType == null) {
                continue;
            }
            rowMap.put(bloodType, row);
            totalCount += toLong(row.get("cnt"));
            wholeBloodTotalCount += toLong(row.get("wholeBloodCount"));
            plateletTotalCount += toLong(row.get("plateletCount"));
            wholeBloodTotalUnit = wholeBloodTotalUnit.add(toBigDecimal(row.get("wholeBloodUnit")));
            plateletTotalUnit = plateletTotalUnit.add(toBigDecimal(row.get("plateletUnit")));
        }

        List<BloodTypeDistributionRespVO.Item> items = new ArrayList<>();
        for (String bloodType : STANDARD_BLOOD_TYPES) {
            Map<String, Object> row = rowMap.get(bloodType);
            BloodTypeDistributionRespVO.Item item = new BloodTypeDistributionRespVO.Item();
            item.setBloodType(bloodType);

            long count = row == null ? 0L : toLong(row.get("cnt"));
            long wholeBloodCount = row == null ? 0L : toLong(row.get("wholeBloodCount"));
            long plateletCount = row == null ? 0L : toLong(row.get("plateletCount"));
            BigDecimal wholeBloodUnit = row == null ? BigDecimal.ZERO : toBigDecimal(row.get("wholeBloodUnit"));
            BigDecimal plateletUnit = row == null ? BigDecimal.ZERO : toBigDecimal(row.get("plateletUnit"));

            item.setCount(count);
            item.setRatio(calcRatio(count, totalCount));
            item.setWholeBloodCount(wholeBloodCount);
            item.setWholeBloodRatio(calcRatio(wholeBloodCount, wholeBloodTotalCount));
            item.setWholeBloodUnit(wholeBloodUnit);
            item.setPlateletCount(plateletCount);
            item.setPlateletRatio(calcRatio(plateletCount, plateletTotalCount));
            item.setPlateletUnit(plateletUnit);
            items.add(item);
        }

        BloodTypeDistributionRespVO resp = new BloodTypeDistributionRespVO();
        resp.setItems(items);
        resp.setTotalCount(totalCount);
        resp.setWholeBloodTotalCount(wholeBloodTotalCount);
        resp.setPlateletTotalCount(plateletTotalCount);
        resp.setWholeBloodTotalUnit(wholeBloodTotalUnit);
        resp.setPlateletTotalUnit(plateletTotalUnit);
        return resp;
    }

    // ==================== 内部方法 ====================

    private List<CollectionTrendRespVO.Item> queryCollectionTrendItems(LocalDateTime[] range, String dateFormat,
            String collectionDepartment, List<String> bloodVolumeConditions, List<String> organizationModes) {
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                dateFormat + " AS period",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt");
        query.between("blood_collection_time", range[0], range[1]);
        query.eqIfPresent("collection_department", collectionDepartment);
        if (!bloodVolumeConditions.isEmpty()) {
            query.and(w -> {
                for (int i = 0; i < bloodVolumeConditions.size(); i++) {
                    if (i == 0) {
                        w.apply(bloodVolumeConditions.get(i));
                    } else {
                        w.or().apply(bloodVolumeConditions.get(i));
                    }
                }
            });
        }
        query.inIfPresent("organization_mode", organizationModes);
        query.groupBy(dateFormat);
        query.orderByAsc("period");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        List<CollectionTrendRespVO.Item> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CollectionTrendRespVO.Item item = new CollectionTrendRespVO.Item();
            item.setPeriod(String.valueOf(row.get("period")));
            item.setTotalUnit(toBigDecimal(row.get("totalUnit")));
            item.setCount(toLong(row.get("cnt")));
            items.add(item);
        }

        return items;
    }

    private List<String> normalizeTrendTypes(List<String> trendTypes) {
        if (trendTypes == null || trendTypes.isEmpty()) {
            return Collections.singletonList(TREND_TYPE_WHOLE_BLOOD);
        }
        Set<String> orderedUnique = new LinkedHashSet<>();
        for (String trendType : trendTypes) {
            if (isSupportedTrendType(trendType)) {
                orderedUnique.add(trendType);
            }
        }
        if (orderedUnique.isEmpty()) {
            return Collections.singletonList(TREND_TYPE_WHOLE_BLOOD);
        }
        return new ArrayList<>(orderedUnique);
    }

    private boolean isSupportedTrendType(String trendType) {
        return TREND_TYPE_VOLUNTARY.equals(trendType)
                || TREND_TYPE_GROUP.equals(trendType)
                || TREND_TYPE_WHOLE_BLOOD.equals(trendType)
                || TREND_TYPE_PLATELET.equals(trendType);
    }

    private List<String> buildConditionsByTrendType(String trendType) {
        if (TREND_TYPE_VOLUNTARY.equals(trendType)) {
            return Collections.singletonList(ORG_MODE_VOLUNTARY_CONDITION);
        }
        if (TREND_TYPE_GROUP.equals(trendType)) {
            return Collections.singletonList(ORG_MODE_GROUP_CONDITION);
        }
        if (TREND_TYPE_PLATELET.equals(trendType)) {
            return Collections.singletonList("blood_volume IS NOT NULL AND " + PLATELET_CONDITION);
        }
        return Collections.singletonList("blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION);
    }

    // TODO 这里需要重新梳理逻辑，大概率是算法投入的数据源不对，改为从mysql数据库中查询聚合
    private List<String> buildBloodVolumeConditions(List<String> trendTypes) {
        return Collections.singletonList("blood_volume IS NOT NULL AND " + PLATELET_CONDITION);
        // boolean containsWholeBlood = trendTypes.contains(TREND_TYPE_WHOLE_BLOOD);
        // boolean containsPlatelet = trendTypes.contains(TREND_TYPE_PLATELET);
        // if (containsWholeBlood && containsPlatelet) {
        // return Collections.emptyList();
        // }
        // if (containsWholeBlood) {
        // return Collections.singletonList("blood_volume IS NOT NULL AND " +
        // WHOLE_BLOOD_CONDITION);
        // }
        // if (containsPlatelet) {
        // return Collections.singletonList("blood_volume IS NOT NULL AND " +
        // PLATELET_CONDITION);
        // }
        // return Collections.emptyList();
    }

    private List<String> buildOrganizationModesByTrendTypes(List<String> trendTypes) {
        boolean containsVoluntary = trendTypes.contains(TREND_TYPE_VOLUNTARY);
        boolean containsGroup = trendTypes.contains(TREND_TYPE_GROUP);
        if (containsVoluntary && containsGroup) {
            return Collections.emptyList();
        }
        if (containsVoluntary) {
            return Arrays.asList(ORG_MODE_PERSONAL, TREND_TYPE_VOLUNTARY);
        }
        if (containsGroup) {
            return Arrays.asList(TREND_TYPE_GROUP, ORG_MODE_HOSPITAL_GROUP);
        }
        return Collections.emptyList();
    }

    private List<String> buildBloodTypeConditions(List<String> bloodTypes) {
        if (bloodTypes == null || bloodTypes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> conditions = new LinkedHashSet<>();
        for (String bt : bloodTypes) {
            if (TREND_TYPE_WHOLE_BLOOD.equals(bt)) {
                conditions.add("blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION);
            } else if (TREND_TYPE_PLATELET.equals(bt)) {
                conditions.add("blood_volume IS NOT NULL AND " + PLATELET_CONDITION);
            }
        }
        if (conditions.isEmpty()) {
            return Collections.singletonList("1 = 0");
        }
        return new ArrayList<>(conditions);
    }

    private String classifyDonationSiteType(String site) {
        if (site == null || site.isEmpty()) {
            return null;
        }
        if (site.contains("方舱") || site.contains("方仓")) {
            return "献血方舱";
        }
        if (site.contains("献血屋")) {
            return "献血屋";
        }
        if (site.contains("献血车")) {
            return "献血车";
        }
        return null;
    }

    private List<DonorDistributionRespVO.AgeDistributionItem> queryAgeDistribution(LocalDateTime[] range,
            long totalCount) {
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "SUM(CASE WHEN age >= 18 AND age <= 25 THEN 1 ELSE 0 END) AS age18to25",
                "SUM(CASE WHEN age >= 26 AND age <= 35 THEN 1 ELSE 0 END) AS age26to35",
                "SUM(CASE WHEN age >= 36 AND age <= 45 THEN 1 ELSE 0 END) AS age36to45",
                "SUM(CASE WHEN age >= 46 AND age <= 55 THEN 1 ELSE 0 END) AS age46to55",
                "SUM(CASE WHEN age >= 56 THEN 1 ELSE 0 END) AS age56plus");
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("age");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);
        Map<String, Object> row = rows.get(0);

        String[][] groups = {
                { "18岁-25岁", "age18to25" },
                { "26岁-35岁", "age26to35" },
                { "36岁-45岁", "age36to45" },
                { "46岁-55岁", "age46to55" },
                { "56岁及以上", "age56plus" }
        };

        List<DonorDistributionRespVO.AgeDistributionItem> items = new ArrayList<>();
        for (String[] group : groups) {
            long count = toLong(row.get(group[1]));
            DonorDistributionRespVO.AgeDistributionItem item = new DonorDistributionRespVO.AgeDistributionItem();
            item.setAgeGroup(group[0]);
            item.setCount(count);
            item.setRatio(calcRatio(count, totalCount));
            items.add(item);
        }
        return items;
    }

    private String classifySiteToAreaType(String site) {
        if (site == null || site.isEmpty()) {
            return "其他";
        }
        // 其他：明确停用/临时/互助
        if (containsAny(site, "临时", "停用", "站内", "互助")) {
            return "其他";
        }
        // 独立设施：献血车
        if (site.contains("献血车")) {
            return "独立设施";
        }
        // 医疗服务区
        if (containsAny(site, "医院", "血库", "血站", "卫生院", "保健院", "采血室", "机采室")) {
            return "医疗服务区";
        }
        // 商业中心
        if (containsAny(site, "商场", "广场", "大厦", "购物中心", "万达", "荟聚", "奥特莱斯", "超市", "卖场", "百货")) {
            return "商业中心";
        }
        // 交通枢纽
        if (containsAny(site, "火车站", "地铁", "高铁", "南站", "北站", "交通枢纽", "西客站")) {
            return "交通枢纽";
        }
        // 旅游景区（"园"放在最后避免误匹配，先判断更精确的关键词）
        if (containsAny(site, "公园", "庙会", "长城", "什刹海", "故宫", "动物园", "植物园", "博览园", "寺", "园")) {
            return "旅游景区";
        }
        // 文化广场
        if (containsAny(site, "文化活动中心", "图书馆", "书店", "博物馆", "党校", "学校", "学院", "办事处", "镇政府")) {
            return "文化广场";
        }
        return "其他";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String mapDepartmentToDistrict(String collectionDepartment) {
        if (collectionDepartment == null) {
            return "未知区域";
        }
        // 去除"北京市"前缀后匹配
        String dept = collectionDepartment.startsWith("北京市")
                ? collectionDepartment.substring(3)
                : collectionDepartment;
        switch (dept) {
            case "红十字血液中心":
                return "城区-海淀区";
            case "通州区中心血站":
                return "通州区";
            case "密云区中心血站":
                return "密云区";
            case "延庆区中心血站":
                return "延庆区";
            case "房山区中心血库":
                return "房山区";
            case "顺义区中心血库":
                return "顺义区";
            case "昌平区中心血库":
                return "昌平区";
            case "大兴区中心血库":
                return "大兴区";
            case "门头沟区中心血库":
                return "门头沟区";
            case "平谷区中心血库":
                return "平谷区";
            case "怀柔区中心血库":
                return "怀柔区";
            default:
                return dept;
        }
    }

    /**
     * 根据统计周期返回 DATE_FORMAT 表达式
     */
    private String getDateFormatExpression(String period) {
        switch (period) {
            case "DAY":
                return "DATE_FORMAT(blood_collection_time, '%Y-%m-%d')";
            case "WEEK":
                return "DATE_FORMAT(blood_collection_time, '%x-W%v')";
            case "MONTH":
                return "DATE_FORMAT(blood_collection_time, '%Y-%m')";
            case "YEAR":
                return "DATE_FORMAT(blood_collection_time, '%Y')";
            default:
                return "DATE_FORMAT(blood_collection_time, '%Y-%m')";
        }
    }

    /**
     * 标准化时间范围：如果未传入，默认最近 3 个月
     */
    private LocalDateTime[] normalizeTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            LocalDateTime now = LocalDateTime.now();
            return new LocalDateTime[] { now.minusMonths(DEFAULT_TIME_RANGE_MONTHS), now };
        }
        return new LocalDateTime[] { startTime, endTime };
    }

    private BigDecimal calcRatio(long count, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }

}
