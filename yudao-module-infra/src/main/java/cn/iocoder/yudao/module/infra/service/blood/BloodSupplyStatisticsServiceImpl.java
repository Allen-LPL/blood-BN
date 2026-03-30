package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodProductSupplyStatsRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.CollectionDistrictSupplyRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.CollectionOrgSupplyRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodUsagePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodUsageRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyStatsRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.DistrictHospitalRankReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.DistrictHospitalRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.TopHospitalBloodSourceReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.TopHospitalBloodSourceRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodHospitalInfoDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodHospitalInfoMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodSupplyFactMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class BloodSupplyStatisticsServiceImpl implements BloodSupplyStatisticsService {

    private static final String PERIOD_FORMAT_DAY = "%Y-%m-%d";
    private static final String PERIOD_FORMAT_MONTH = "%Y-%m";
    private static final String PERIOD_FORMAT_YEAR = "%Y";

    @Resource
    private BloodHospitalInfoMapper bloodHospitalInfoMapper;

    @Resource
    private BloodSupplyFactMapper bloodSupplyFactMapper;

    @Override
    public List<HospitalSupplyStatsRespVO> getHospitalSupplyStats(LocalDateTime startTime, LocalDateTime endTime,
            List<String> districts) {
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectNonMilitaryHospitalsByDistricts(districts);
        Map<String, Long> countMap = bloodSupplyFactMapper.selectReceivingOrgCountMap(startTime, endTime, districts);

        List<HospitalSupplyStatsRespVO> result = new ArrayList<>();
        for (BloodHospitalInfoDO hospital : hospitals) {
            result.add(HospitalSupplyStatsRespVO.builder()
                    .yiYuanQuanCheng(hospital.getYiYuanQuanCheng())
                    .jiGouShuXing(hospital.getJiGouShuXing())
                    .supplyCount(countMap.getOrDefault(hospital.getYiYuanQuanCheng(), 0L))
                    .build());
        }
        return result;
    }

    @Override
    public List<HospitalSupplyAggRespVO> getHospitalSupplyAgg(LocalDateTime startTime, LocalDateTime endTime,
            List<String> districts, List<String> bloodProductTypes,
            String period) {
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectNonMilitaryHospitalsByDistricts(districts);

        String periodFormat = resolvePeriodFormat(period);
        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectOrgPeriodProductCounts(
                startTime, endTime, districts, periodFormat);

        // 构建：orgName -> period -> periodItem 构建器
        Map<String, Map<String, long[]>> orgPeriodMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("receiving_org");
            String timePeriod = (String) row.get("time_period");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            String category = classifyProductName(productName);
            if (!isTypeIncluded(category, bloodProductTypes)) {
                continue;
            }

            orgPeriodMap.computeIfAbsent(org, k -> new LinkedHashMap<>())
                    .computeIfAbsent(timePeriod, k -> new long[4]); // [red, platelet, plasma, total]

            long[] counts = orgPeriodMap.get(org).get(timePeriod);
            switch (category) {
                case "红细胞类":
                    counts[0] += cnt;
                    break;
                case "血小板类":
                    counts[1] += cnt;
                    break;
                case "血浆类":
                    counts[2] += cnt;
                    break;
                default:
                    break;
            }
            counts[3] += cnt;
        }

        List<HospitalSupplyAggRespVO> result = new ArrayList<>();
        for (BloodHospitalInfoDO hospital : hospitals) {
            String name = hospital.getYiYuanQuanCheng();
            Map<String, long[]> periodData = orgPeriodMap.getOrDefault(name, new LinkedHashMap<>());

            List<HospitalSupplyAggRespVO.PeriodItem> periodItems = new ArrayList<>();
            for (Map.Entry<String, long[]> entry : periodData.entrySet()) {
                long[] c = entry.getValue();
                periodItems.add(HospitalSupplyAggRespVO.PeriodItem.builder()
                        .period(entry.getKey())
                        .redBloodCellCount(c[0])
                        .plateletCount(c[1])
                        .plasmaCount(c[2])
                        .totalCount(c[3])
                        .build());
            }

            result.add(HospitalSupplyAggRespVO.builder()
                    .yiYuanQuanCheng(name)
                    .jiGouShuXing(hospital.getJiGouShuXing())
                    .periods(periodItems)
                    .build());
        }
        return result;
    }

    @Override
    public List<CollectionOrgSupplyRankRespVO> getCollectionOrgSupplyRank(LocalDateTime startTime,
            LocalDateTime endTime,
            List<String> districts,
            List<String> bloodProductTypes) {
        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectIssuingOrgProductCounts(
                startTime, endTime, districts);

        // 聚合：issuesOrg -> [红色、血小板、血浆、总计]
        Map<String, long[]> orgCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("issuing_org");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            String category = classifyProductName(productName);
            if (!isTypeIncluded(category, bloodProductTypes)) {
                continue;
            }

            long[] counts = orgCountMap.computeIfAbsent(org, k -> new long[4]);
            switch (category) {
                case "红细胞类":
                    counts[0] += cnt;
                    break;
                case "血小板类":
                    counts[1] += cnt;
                    break;
                case "血浆类":
                    counts[2] += cnt;
                    break;
                default:
                    break;
            }
            counts[3] += cnt;
        }

        List<CollectionOrgSupplyRankRespVO> result = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : orgCountMap.entrySet()) {
            long[] c = entry.getValue();
            result.add(CollectionOrgSupplyRankRespVO.builder()
                    .issuingOrg(entry.getKey())
                    .redBloodCellCount(c[0])
                    .plateletCount(c[1])
                    .plasmaCount(c[2])
                    .totalCount(c[3])
                    .build());
        }
        result.sort((a, b) -> Long.compare(b.getTotalCount(), a.getTotalCount()));

        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        return result;
    }

    private static final List<String> KNOWN_STATIONS = Arrays.asList(
            "北京市红十字血液中心", "通州区中心血站", "密云区中心血站", "延庆区中心血站",
            "房山区中心血库", "顺义区中心血库", "昌平区中心血库", "大兴区中心血库",
            "门头沟区中心血库", "平谷区中心血库", "怀柔区中心血库");

    @Override
    public CollectionDistrictSupplyRankRespVO getCollectionDistrictSupplyRank(LocalDateTime startTime,
            LocalDateTime endTime,
            List<String> districts,
            List<String> bloodProductTypes) {
        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectIssuingOrgDistrictProductCounts(
                startTime, endTime, districts);

        // 构建：issuesOrg -> District -> long[4]（红色、血小板、血浆、总计）
        Map<String, Map<String, long[]>> orgDistrictMap = new LinkedHashMap<>();
        // 构建：district -> long[4]（红色、血小板、血浆、总计），用于计算各站点在各行政区的供血占比
        Map<String, long[]> districtTotalMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            String org = (String) row.get("issuing_org");
            String district = (String) row.get("receiving_org_admin_region");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            if (org == null || district == null) {
                continue;
            }

            String category = classifyProductName(productName);
            if (!isTypeIncluded(category, bloodProductTypes)) {
                continue;
            }

            long[] orgDistCounts = orgDistrictMap
                    .computeIfAbsent(org, k -> new LinkedHashMap<>())
                    .computeIfAbsent(district, k -> new long[4]);
            accumulateCounts(orgDistCounts, category, cnt);

            long[] distCounts = districtTotalMap.computeIfAbsent(district, k -> new long[4]);
            accumulateCounts(distCounts, category, cnt);
        }

        // 1. 地区排名按总计数降序排列
        List<CollectionDistrictSupplyRankRespVO.DistrictRankItem> districtRanks = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : districtTotalMap.entrySet()) {
            long[] c = entry.getValue();
            districtRanks.add(CollectionDistrictSupplyRankRespVO.DistrictRankItem.builder()
                    .district(entry.getKey())
                    .redBloodCellCount(c[0])
                    .plateletCount(c[1])
                    .plasmaCount(c[2])
                    .totalCount(c[3])
                    .build());
        }
        districtRanks.sort((a, b) -> Long.compare(b.getTotalCount(), a.getTotalCount()));
        for (int i = 0; i < districtRanks.size(); i++) {
            districtRanks.get(i).setRank(i + 1);
        }

        // 2. 站点-地区供血总量（固定 11 个已知站点）
        List<CollectionDistrictSupplyRankRespVO.StationDistrictSupplyItem> stationDistrictSupplies = new ArrayList<>();
        for (String station : KNOWN_STATIONS) {
            Map<String, long[]> districtData = orgDistrictMap.getOrDefault(station, new LinkedHashMap<>());
            List<CollectionDistrictSupplyRankRespVO.StationDistrictSupplyItem.DistrictSupply> supplies = new ArrayList<>();
            for (Map.Entry<String, long[]> entry : districtData.entrySet()) {
                long[] c = entry.getValue();
                supplies.add(CollectionDistrictSupplyRankRespVO.StationDistrictSupplyItem.DistrictSupply.builder()
                        .district(entry.getKey())
                        .redBloodCellCount(c[0])
                        .plateletCount(c[1])
                        .plasmaCount(c[2])
                        .totalCount(c[3])
                        .build());
            }
            stationDistrictSupplies.add(CollectionDistrictSupplyRankRespVO.StationDistrictSupplyItem.builder()
                    .issuingOrg(station)
                    .districtSupplies(supplies)
                    .build());
        }

        // 预计算各站点对所有行政区的总供血量
        Map<String, Long> stationTotalMap = new LinkedHashMap<>();
        for (String station : KNOWN_STATIONS) {
            Map<String, long[]> districtData = orgDistrictMap.getOrDefault(station, new LinkedHashMap<>());
            long stationTotal = 0L;
            for (long[] c : districtData.values()) {
                stationTotal += c[3];
            }
            stationTotalMap.put(station, stationTotal);
        }

        // 3. 站点-地区供血占比
        //    districtPercentage    = stationSupplyCount / districtTotalCount（该站点在该行政区的占比）
        //    stationOutputPercentage = stationSupplyCount / stationTotalCount（该行政区占该站点总输出的比例）
        List<CollectionDistrictSupplyRankRespVO.StationDistrictPercentItem> stationDistrictPercents = new ArrayList<>();
        for (String station : KNOWN_STATIONS) {
            Map<String, long[]> districtData = orgDistrictMap.getOrDefault(station, new LinkedHashMap<>());
            long stationTotal = stationTotalMap.getOrDefault(station, 0L);
            List<CollectionDistrictSupplyRankRespVO.StationDistrictPercentItem.DistrictPercent> percents = new ArrayList<>();
            for (Map.Entry<String, long[]> entry : districtData.entrySet()) {
                String district = entry.getKey();
                long stationCount = entry.getValue()[3];
                long districtTotal = districtTotalMap.containsKey(district)
                        ? districtTotalMap.get(district)[3]
                        : 0L;
                double districtPct = districtTotal > 0
                        ? Math.round((double) stationCount / districtTotal * 10000.0) / 100.0
                        : 0.0;
                double outputPct = stationTotal > 0
                        ? Math.round((double) stationCount / stationTotal * 10000.0) / 100.0
                        : 0.0;
                percents.add(CollectionDistrictSupplyRankRespVO.StationDistrictPercentItem.DistrictPercent.builder()
                        .district(district)
                        .stationSupplyCount(stationCount)
                        .districtTotalCount(districtTotal)
                        .districtPercentage(districtPct)
                        .stationTotalCount(stationTotal)
                        .stationOutputPercentage(outputPct)
                        .build());
            }
            stationDistrictPercents.add(CollectionDistrictSupplyRankRespVO.StationDistrictPercentItem.builder()
                    .issuingOrg(station)
                    .districtPercents(percents)
                    .build());
        }

        return CollectionDistrictSupplyRankRespVO.builder()
                .districtRanks(districtRanks)
                .stationDistrictSupplies(stationDistrictSupplies)
                .stationDistrictPercents(stationDistrictPercents)
                .build();
    }

    @Override
    public List<BloodProductSupplyStatsRespVO> getBloodProductSupplyStats(LocalDateTime startTime,
                                                                           LocalDateTime endTime,
                                                                           List<String> districts,
                                                                           List<String> bloodProductTypes) {
        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectProductNameCounts(
                startTime, endTime, districts);

        List<BloodProductSupplyStatsRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            String category = classifyProductName(productName);
            if (!isTypeIncluded(category, bloodProductTypes)) {
                continue;
            }

            result.add(BloodProductSupplyStatsRespVO.builder()
                    .bloodProductName(productName)
                    .category(category)
                    .supplyCount(cnt)
                    .build());
        }
        return result;
    }

    @Override
    public PageResult<HospitalBloodUsageRespVO> getHospitalBloodUsagePage(HospitalBloodUsagePageReqVO reqVO) {
        // 1. 按条件查询医院列表（非部队医院，支持行政区、机构属性、医院类型过滤）
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectByFilters(
                reqVO.getDistricts(), reqVO.getJiGouShuXing(), reqVO.getYiYuanLeiXing());

        if (hospitals.isEmpty()) {
            return PageResult.empty(0L);
        }

        // 2. 取医院名称列表，查询对应供血事实聚合数据
        List<String> orgNames = hospitals.stream()
                .map(BloodHospitalInfoDO::getYiYuanQuanCheng)
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectReceivingOrgProductCountsByOrgs(
                reqVO.getStartTime(), reqVO.getEndTime(), orgNames);

        // 3. 聚合：orgName -> [红细胞, 血小板, 血浆, 合计]
        Map<String, long[]> orgCountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("receiving_org");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            String category = classifyProductName(productName);
            long[] counts = orgCountMap.computeIfAbsent(org, k -> new long[4]);
            accumulateCounts(counts, category, cnt);
        }

        // 4. 构建完整结果列表（所有医院，无数据的填0）
        List<HospitalBloodUsageRespVO> allResults = new ArrayList<>(hospitals.size());
        for (BloodHospitalInfoDO hospital : hospitals) {
            long[] c = orgCountMap.getOrDefault(hospital.getYiYuanQuanCheng(), new long[4]);
            allResults.add(HospitalBloodUsageRespVO.builder()
                    .yiYuanQuanCheng(hospital.getYiYuanQuanCheng())
                    .xingZhengQuYu(hospital.getXingZhengQuYu())
                    .jiGouShuXing(hospital.getJiGouShuXing())
                    .jiGouLeiBie(hospital.getJiGouLeiBie())
                    .yiYuanLeiXing(hospital.getYiYuanLeiXing())
                    .redBloodCellCount(c[0])
                    .plateletCount(c[1])
                    .plasmaCount(c[2])
                    .totalCount(c[3])
                    .build());
        }

        // 5. 内存分页
        long total = allResults.size();
        int fromIndex = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        if (fromIndex >= total) {
            return PageResult.empty(total);
        }
        int toIndex = (int) Math.min(fromIndex + reqVO.getPageSize(), total);
        return new PageResult<>(allResults.subList(fromIndex, toIndex), total);
    }

    @Override
    public TopHospitalBloodSourceRespVO getTopHospitalBloodSource(TopHospitalBloodSourceReqVO reqVO) {
        // 1. 按条件查询医院列表（非部队医院，支持行政区、机构属性过滤）
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectByFilters(
                reqVO.getDistricts(), reqVO.getJiGouShuXing(), null);

        if (hospitals.isEmpty()) {
            return TopHospitalBloodSourceRespVO.builder()
                    .topHospitalName(null)
                    .topHospitalTotalCount(0L)
                    .sources(new ArrayList<>())
                    .build();
        }

        // 2. 查询各医院用血量，找出总用血量最高的医院（倒排第一）
        List<String> orgNames = hospitals.stream()
                .map(BloodHospitalInfoDO::getYiYuanQuanCheng)
                .collect(Collectors.toList());

        List<Map<String, Object>> usageRows = bloodSupplyFactMapper.selectReceivingOrgProductCountsByOrgs(
                reqVO.getStartTime(), reqVO.getEndTime(), orgNames);

        Map<String, Long> hospitalTotalMap = new HashMap<>();
        for (Map<String, Object> row : usageRows) {
            String org = (String) row.get("receiving_org");
            long cnt = ((Number) row.get("cnt")).longValue();
            hospitalTotalMap.merge(org, cnt, Long::sum);
        }

        String topHospital = null;
        long topTotal = -1L;
        for (Map.Entry<String, Long> entry : hospitalTotalMap.entrySet()) {
            if (entry.getValue() > topTotal) {
                topTotal = entry.getValue();
                topHospital = entry.getKey();
            }
        }

        if (topHospital == null) {
            return TopHospitalBloodSourceRespVO.builder()
                    .topHospitalName(null)
                    .topHospitalTotalCount(0L)
                    .sources(new ArrayList<>())
                    .build();
        }

        // 3. 查询该医院的供血来源，按 issuing_org + blood_product_name 聚合
        List<Map<String, Object>> sourceRows = bloodSupplyFactMapper.selectIssuingOrgProductCountsByReceivingOrg(
                topHospital, reqVO.getStartTime(), reqVO.getEndTime());

        // 4. 聚合：issuingOrg -> [红细胞, 血小板, 血浆, 合计]
        Map<String, long[]> issuingOrgMap = new LinkedHashMap<>();
        for (Map<String, Object> row : sourceRows) {
            String issuingOrg = (String) row.get("issuing_org");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();

            if (issuingOrg == null) {
                continue;
            }
            String category = classifyProductName(productName);
            long[] counts = issuingOrgMap.computeIfAbsent(issuingOrg, k -> new long[4]);
            accumulateCounts(counts, category, cnt);
        }

        // 5. 构建结果列表，按总量倒序排名
        List<TopHospitalBloodSourceRespVO.SourceItem> sources = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : issuingOrgMap.entrySet()) {
            long[] c = entry.getValue();
            sources.add(TopHospitalBloodSourceRespVO.SourceItem.builder()
                    .issuingOrg(entry.getKey())
                    .redBloodCellCount(c[0])
                    .plateletCount(c[1])
                    .plasmaCount(c[2])
                    .totalCount(c[3])
                    .build());
        }
        sources.sort((a, b) -> Long.compare(b.getTotalCount(), a.getTotalCount()));
        for (int i = 0; i < sources.size(); i++) {
            sources.get(i).setRank(i + 1);
        }

        return TopHospitalBloodSourceRespVO.builder()
                .topHospitalName(topHospital)
                .topHospitalTotalCount(topTotal)
                .sources(sources)
                .build();
    }

    @Override
    public List<DistrictHospitalRankRespVO> getDistrictHospitalRank(DistrictHospitalRankReqVO reqVO) {
        // 1. 查询符合条件的医院（非部队医院，支持行政区过滤），获取名称→行政区映射
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectNonMilitaryHospitalsByDistricts(
                reqVO.getDistricts());

        if (hospitals.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查询这些医院的供血事实聚合数据
        List<String> orgNames = hospitals.stream()
                .map(BloodHospitalInfoDO::getYiYuanQuanCheng)
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectReceivingOrgProductCountsByOrgs(
                reqVO.getStartTime(), reqVO.getEndTime(), orgNames);

        // 3. 聚合：orgName → long[4]
        Map<String, long[]> orgCountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("receiving_org");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();
            String category = classifyProductName(productName);
            long[] counts = orgCountMap.computeIfAbsent(org, k -> new long[4]);
            accumulateCounts(counts, category, cnt);
        }

        // 4. 按行政区分组：district → list of HospitalItem
        Map<String, List<DistrictHospitalRankRespVO.HospitalItem>> districtHospitalMap = new LinkedHashMap<>();
        Map<String, long[]> districtCountMap = new LinkedHashMap<>();

        for (BloodHospitalInfoDO hospital : hospitals) {
            String name = hospital.getYiYuanQuanCheng();
            String district = hospital.getXingZhengQuYu();
            long[] c = orgCountMap.getOrDefault(name, new long[4]);

            districtHospitalMap.computeIfAbsent(district, k -> new ArrayList<>())
                    .add(DistrictHospitalRankRespVO.HospitalItem.builder()
                            .hospitalName(name)
                            .redBloodCellCount(c[0])
                            .plateletCount(c[1])
                            .plasmaCount(c[2])
                            .totalCount(c[3])
                            .build());

            long[] dc = districtCountMap.computeIfAbsent(district, k -> new long[4]);
            dc[0] += c[0];
            dc[1] += c[1];
            dc[2] += c[2];
            dc[3] += c[3];
        }

        // 5. 各行政区内医院按总量倒序排名
        for (List<DistrictHospitalRankRespVO.HospitalItem> list : districtHospitalMap.values()) {
            list.sort((a, b) -> Long.compare(b.getTotalCount(), a.getTotalCount()));
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setRank(i + 1);
            }
        }

        // 6. 构建行政区条目列表，按行政区总量倒序排名
        List<DistrictHospitalRankRespVO> result = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : districtCountMap.entrySet()) {
            String district = entry.getKey();
            long[] dc = entry.getValue();
            result.add(DistrictHospitalRankRespVO.builder()
                    .district(district)
                    .districtRedBloodCellCount(dc[0])
                    .districtPlateletCount(dc[1])
                    .districtPlasmaCount(dc[2])
                    .districtTotalCount(dc[3])
                    .hospitals(districtHospitalMap.get(district))
                    .build());
        }
        result.sort((a, b) -> Long.compare(b.getDistrictTotalCount(), a.getDistrictTotalCount()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }

        return result;
    }

    private static final String DIM_JI_GOU_SHU_XING        = "机构属性";
    private static final String DIM_JI_GOU_LEI_BIE         = "机构类别";
    private static final String DIM_YI_YUAN_LEI_XING       = "医院类型";
    private static final String DIM_ZhuanYe                = "医院专业类型";

    @Override
    public HospitalBloodDistributionRespVO getHospitalBloodDistribution(HospitalBloodDistributionReqVO reqVO) {
        // 1. 查询符合条件的所有医院（含部队医院）
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectAllByDistributionFilters(
                reqVO.getJiGouShuXing(), reqVO.getJiGouLeiBie(),
                reqVO.getYiYuanLeiXing(), reqVO.getYiYuanZhuanYeLeiXing());

        if (hospitals.isEmpty()) {
            return HospitalBloodDistributionRespVO.builder()
                    .groupByDimension(resolveGroupByLabel(reqVO))
                    .totalHospitalCount(0L).totalBloodCount(0L)
                    .totalRedBloodCellCount(0L).totalPlateletCount(0L).totalPlasmaCount(0L)
                    .items(new ArrayList<>())
                    .build();
        }

        // 2. 确定分组维度
        String groupByField = resolveGroupByField(reqVO);

        // 3. 查询供血事实聚合数据
        List<String> orgNames = hospitals.stream()
                .map(BloodHospitalInfoDO::getYiYuanQuanCheng)
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = bloodSupplyFactMapper.selectReceivingOrgProductCountsByOrgs(
                reqVO.getStartTime(), reqVO.getEndTime(), orgNames);

        Map<String, long[]> orgBloodMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("receiving_org");
            String productName = (String) row.get("blood_product_name");
            long cnt = ((Number) row.get("cnt")).longValue();
            String cat = classifyProductName(productName);
            long[] counts = orgBloodMap.computeIfAbsent(org, k -> new long[4]);
            accumulateCounts(counts, cat, cnt);
        }

        // 4. 按分组维度汇总：category → [医院数, 红细胞, 血小板, 血浆, 合计]
        Map<String, long[]> categoryMap = new LinkedHashMap<>(); // long[5]: [hospCnt, red, platelet, plasma, total]
        for (BloodHospitalInfoDO hospital : hospitals) {
            String category = getCategoryValue(hospital, groupByField);
            if (category == null || category.trim().isEmpty()) {
                category = "其他";
            }
            long[] c = categoryMap.computeIfAbsent(category, k -> new long[5]);
            c[0]++;  // hospital count

            long[] blood = orgBloodMap.getOrDefault(hospital.getYiYuanQuanCheng(), new long[4]);
            c[1] += blood[0];
            c[2] += blood[1];
            c[3] += blood[2];
            c[4] += blood[3];
        }

        // 5. 计算总量
        long totalHosp = hospitals.size();
        long totalBlood = 0L, totalRed = 0L, totalPlatelet = 0L, totalPlasma = 0L;
        for (long[] c : categoryMap.values()) {
            totalBlood   += c[4];
            totalRed     += c[1];
            totalPlatelet += c[2];
            totalPlasma  += c[3];
        }

        // 6. 构建明细列表，按供血量倒序
        List<HospitalBloodDistributionRespVO.DistributionItem> items = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : categoryMap.entrySet()) {
            long[] c = entry.getValue();
            double hospRatio  = totalHosp  > 0 ? Math.round((double) c[0] / totalHosp  * 10000.0) / 100.0 : 0.0;
            double bloodRatio = totalBlood > 0 ? Math.round((double) c[4] / totalBlood * 10000.0) / 100.0 : 0.0;
            items.add(HospitalBloodDistributionRespVO.DistributionItem.builder()
                    .category(entry.getKey())
                    .hospitalCount(c[0])
                    .hospitalRatio(hospRatio)
                    .redBloodCellCount(c[1])
                    .plateletCount(c[2])
                    .plasmaCount(c[3])
                    .bloodCount(c[4])
                    .bloodRatio(bloodRatio)
                    .build());
        }
        items.sort((a, b) -> Long.compare(b.getBloodCount(), a.getBloodCount()));

        return HospitalBloodDistributionRespVO.builder()
                .groupByDimension(resolveGroupByLabel(reqVO))
                .totalHospitalCount(totalHosp)
                .totalBloodCount(totalBlood)
                .totalRedBloodCellCount(totalRed)
                .totalPlateletCount(totalPlatelet)
                .totalPlasmaCount(totalPlasma)
                .items(items)
                .build();
    }

    private String resolveGroupByField(HospitalBloodDistributionReqVO reqVO) {
        boolean hasJiGouShuXing = hasValue(reqVO.getJiGouShuXing());
        boolean hasYiYuanLeiXing = hasValue(reqVO.getYiYuanLeiXing());
        if (hasJiGouShuXing && !hasValue(reqVO.getJiGouLeiBie())) {
            return "jiGouLeiBie";
        }
        if (hasYiYuanLeiXing && !hasValue(reqVO.getYiYuanZhuanYeLeiXing())) {
            return "yiYuanZhuanYeLeiXing";
        }
        if (hasValue(reqVO.getJiGouLeiBie())) {
            return "jiGouLeiBie";
        }
        if (hasValue(reqVO.getYiYuanZhuanYeLeiXing())) {
            return "yiYuanZhuanYeLeiXing";
        }
        return "jiGouShuXing"; // 默认
    }

    private String resolveGroupByLabel(HospitalBloodDistributionReqVO reqVO) {
        switch (resolveGroupByField(reqVO)) {
            case "jiGouLeiBie":         return DIM_JI_GOU_LEI_BIE;
            case "yiYuanZhuanYeLeiXing": return DIM_ZhuanYe;
            case "yiYuanLeiXing":       return DIM_YI_YUAN_LEI_XING;
            default:                    return DIM_JI_GOU_SHU_XING;
        }
    }

    private String getCategoryValue(BloodHospitalInfoDO hospital, String field) {
        switch (field) {
            case "jiGouShuXing":         return hospital.getJiGouShuXing();
            case "jiGouLeiBie":          return hospital.getJiGouLeiBie();
            case "yiYuanLeiXing":        return hospital.getYiYuanLeiXing();
            case "yiYuanZhuanYeLeiXing": return hospital.getYiYuanZhuanYeLeiXing();
            default:                     return hospital.getJiGouShuXing();
        }
    }

    private boolean hasValue(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void accumulateCounts(long[] counts, String category, long cnt) {
        switch (category) {
            case "红细胞类":
                counts[0] += cnt;
                break;
            case "血小板类":
                counts[1] += cnt;
                break;
            case "血浆类":
                counts[2] += cnt;
                break;
            default:
                break;
        }
        counts[3] += cnt;
    }

    private String resolvePeriodFormat(String period) {
        if ("DAY".equals(period)) {
            return PERIOD_FORMAT_DAY;
        } else if ("YEAR".equals(period)) {
            return PERIOD_FORMAT_YEAR;
        }
        return PERIOD_FORMAT_MONTH;
    }

    private String classifyProductName(String productName) {
        if (productName == null) {
            return "其他";
        }
        if (productName.contains("红细胞")) {
            return "红细胞类";
        } else if (productName.contains("血小板")) {
            return "血小板类";
        } else if (productName.contains("血浆")) {
            return "血浆类";
        }
        return "其他";
    }

    private boolean isTypeIncluded(String category, List<String> bloodProductTypes) {
        if (bloodProductTypes == null || bloodProductTypes.isEmpty()) {
            return true;
        }
        return bloodProductTypes.contains(category);
    }

}
