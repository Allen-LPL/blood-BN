package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionMainSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionFactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionMainSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodSupplyFactMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
public class BloodStatisticsServiceImpl implements BloodStatisticsService {

    private static final int DEFAULT_TIME_RANGE_MONTHS = 3;

    /** blood_volume（varchar）转为数值后，< 50 视为单采血小板，>= 50 视为全血 */
    private static final String WHOLE_BLOOD_CONDITION = "CAST(blood_volume AS DECIMAL(10,2)) >= 50";
    private static final String PLATELET_CONDITION = "CAST(blood_volume AS DECIMAL(10,2)) < 50";

    @Resource
    private BloodCollectionFactMapper collectionMapper;

    @Resource
    private BloodCollectionSiteMapper siteMapper;

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
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION + " THEN 1 ELSE 0 END) AS wholeBloodCount",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION + " THEN 1 ELSE 0 END) AS plateletCount",
                "SUM(CASE WHEN gender = '男' THEN 1 ELSE 0 END) AS maleCount",
                "SUM(CASE WHEN gender = '女' THEN 1 ELSE 0 END) AS femaleCount"
        );
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
        return resp;
    }

    // ==================== 2. 全血采血量趋势 ====================

    @Override
    public CollectionTrendRespVO getWholeBloodTrend(LocalDateTime startTime, LocalDateTime endTime, String period) {
        return getCollectionTrend(startTime, endTime, period, WHOLE_BLOOD_CONDITION);
    }

    // ==================== 3. 单采血小板采血量趋势 ====================

    @Override
    public CollectionTrendRespVO getPlateletTrend(LocalDateTime startTime, LocalDateTime endTime, String period) {
        return getCollectionTrend(startTime, endTime, period, PLATELET_CONDITION);
    }

    // ==================== 4. 北京各区采血量 ====================

    @Override
    public DistrictCollectionRespVO getDistrictCollection(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 1) 查询采血点 → 区映射
        List<BloodCollectionSiteDO> sites = siteMapper.selectList(
                new LambdaQueryWrapper<BloodCollectionSiteDO>()
                        .eq(BloodCollectionSiteDO::getState, 1));
        Map<String, String> siteToDistrict = new HashMap<>();
        for (BloodCollectionSiteDO site : sites) {
            siteToDistrict.put(site.getCollectionSiteName(), site.getDistrict());
        }

        // 2) 按采血地点聚合
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_site AS collectionSite",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt"
        );
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_site");
        query.groupBy("collection_site");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        // 3) 按区汇总
        Map<String, BigDecimal> districtUnit = new LinkedHashMap<>();
        Map<String, Long> districtCount = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String site = (String) row.get("collectionSite");
            String district = siteToDistrict.getOrDefault(site, "未知区域");
            BigDecimal unit = toBigDecimal(row.get("totalUnit"));
            long cnt = toLong(row.get("cnt"));
            districtUnit.merge(district, unit, BigDecimal::add);
            districtCount.merge(district, cnt, Long::sum);
        }

        // 4) 构建响应
        List<DistrictCollectionRespVO.Item> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : districtUnit.entrySet()) {
            DistrictCollectionRespVO.Item item = new DistrictCollectionRespVO.Item();
            item.setDistrict(entry.getKey());
            item.setTotalUnit(entry.getValue());
            item.setCount(districtCount.get(entry.getKey()));
            items.add(item);
        }
        // 按采血量降序
        items.sort((a, b) -> b.getTotalUnit().compareTo(a.getTotalUnit()));

        DistrictCollectionRespVO resp = new DistrictCollectionRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 5. 各血液中心排名 ====================

    @Override
    public CenterRankingRespVO getCenterRanking(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "collection_department AS centerName",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt"
        );
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("collection_department");
        query.groupBy("collection_department");
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

        CenterRankingRespVO resp = new CenterRankingRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 6. 每季度采血量统计 ====================

    @Override
    public QuarterlyCollectionRespVO getQuarterlySummary(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "YEAR(blood_collection_time) AS y",
                "QUARTER(blood_collection_time) AS q",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION + " THEN base_unit_value ELSE 0 END) AS wholeBloodUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION + " THEN base_unit_value ELSE 0 END) AS plateletUnit",
                "COUNT(*) AS totalCount"
        );
        query.between("blood_collection_time", range[0], range[1]);
        query.groupBy("YEAR(blood_collection_time)", "QUARTER(blood_collection_time)");
        query.orderByAsc("y", "q");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        List<QuarterlyCollectionRespVO.Item> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            QuarterlyCollectionRespVO.Item item = new QuarterlyCollectionRespVO.Item();
            item.setQuarter(row.get("y") + "-Q" + row.get("q"));
            item.setWholeBloodUnit(toBigDecimal(row.get("wholeBloodUnit")));
            item.setPlateletUnit(toBigDecimal(row.get("plateletUnit")));
            item.setTotalCount(toLong(row.get("totalCount")));
            items.add(item);
        }

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
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + WHOLE_BLOOD_CONDITION + " THEN base_unit_value ELSE 0 END) AS wholeBloodUnit",
                "SUM(CASE WHEN blood_volume IS NOT NULL AND " + PLATELET_CONDITION + " THEN base_unit_value ELSE 0 END) AS plateletUnit",
                "SUM(CASE WHEN gender = '男' THEN 1 ELSE 0 END) AS maleCount",
                "SUM(CASE WHEN gender = '女' THEN 1 ELSE 0 END) AS femaleCount"
        );
        collectionQuery.between("blood_collection_time", range[0], range[1]);
        List<Map<String, Object>> collectionRows = collectionMapper.selectMaps(collectionQuery);
        Map<String, Object> cRow = collectionRows.get(0);

        // 2) 供血统计
        QueryWrapperX<BloodSupplyFactDO> supplyQuery = new QueryWrapperX<>();
        supplyQuery.select(
                "COUNT(*) AS totalCount",
                "SUM(base_unit_value) AS totalUnit"
        );
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
                "SUM(base_unit_value) AS totalUnit"
        );
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
                "SUM(base_unit_value) AS totalUnit"
        );
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
            item.setCollectionOperatingOrg(site.getCollectionOperatingOrg());
            item.setType(site.getType());
            item.setState(site.getState());
            item.setCoordinateLng(site.getCoordinateLng());
            item.setCoordinateLat(site.getCoordinateLat());
            item.setSupplyTotalUnit(supplyMap.getOrDefault(site.getCollectionOperatingOrg(), BigDecimal.ZERO));
            item.setCollectionTotalUnit(collectionMap.getOrDefault(site.getCollectionOperatingOrg(), BigDecimal.ZERO));
            items.add(item);
        }

        MainSiteStatsRespVO resp = new MainSiteStatsRespVO();
        resp.setItems(items);
        return resp;
    }

    // ==================== 9. 采血表血型占比统计 ====================

    @Override
    public BloodTypeDistributionRespVO getBloodTypeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);

        // 1) 查询总数
        QueryWrapperX<BloodCollectionFactDO> totalQuery = new QueryWrapperX<>();
        totalQuery.select("COUNT(*) AS totalCount");
        totalQuery.between("blood_collection_time", range[0], range[1]);
        totalQuery.isNotNull("precheck_blood_type");
        List<Map<String, Object>> totalRows = collectionMapper.selectMaps(totalQuery);
        long totalCount = toLong(totalRows.get(0).get("totalCount"));

        // 2) 按血型分组统计
        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                "precheck_blood_type AS bloodType",
                "COUNT(*) AS cnt"
        );
        query.between("blood_collection_time", range[0], range[1]);
        query.isNotNull("precheck_blood_type");
        query.groupBy("precheck_blood_type");
        query.orderByDesc("cnt");

        List<Map<String, Object>> rows = collectionMapper.selectMaps(query);

        // 3) 构建响应
        List<BloodTypeDistributionRespVO.Item> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            BloodTypeDistributionRespVO.Item item = new BloodTypeDistributionRespVO.Item();
            item.setBloodType((String) row.get("bloodType"));
            long count = toLong(row.get("cnt"));
            item.setCount(count);
            item.setRatio(calcRatio(count, totalCount));
            items.add(item);
        }

        BloodTypeDistributionRespVO resp = new BloodTypeDistributionRespVO();
        resp.setItems(items);
        resp.setTotalCount(totalCount);
        return resp;
    }

    // ==================== 内部方法 ====================

    private CollectionTrendRespVO getCollectionTrend(LocalDateTime startTime, LocalDateTime endTime,
                                                     String period, String bloodTypeCondition) {
        LocalDateTime[] range = normalizeTimeRange(startTime, endTime);
        String dateFormat = getDateFormatExpression(period);

        QueryWrapperX<BloodCollectionFactDO> query = new QueryWrapperX<>();
        query.select(
                dateFormat + " AS period",
                "SUM(base_unit_value) AS totalUnit",
                "COUNT(*) AS cnt"
        );
        query.between("blood_collection_time", range[0], range[1]);
        query.apply("blood_volume IS NOT NULL AND " + bloodTypeCondition);
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

        CollectionTrendRespVO resp = new CollectionTrendRespVO();
        resp.setItems(items);
        return resp;
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
            return new LocalDateTime[]{now.minusMonths(DEFAULT_TIME_RANGE_MONTHS), now};
        }
        return new LocalDateTime[]{startTime, endTime};
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
