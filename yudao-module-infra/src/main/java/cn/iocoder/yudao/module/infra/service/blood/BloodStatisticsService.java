package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*;

import java.time.LocalDateTime;
import java.util.List;

public interface BloodStatisticsService {

    /**
     * 献血者分布统计（全血、单采血小板、男性、女性）
     */
    DonorDistributionRespVO getDonorDistribution(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 全血采血量趋势（按日/周/月/年）
     */
    CollectionTrendRespVO getWholeBloodTrend(LocalDateTime startTime, LocalDateTime endTime, String period,
            List<String> trendTypes);

    /**
     * 采血量情况（按日/周/月/年）
     */
    CollectionTrendRespVO getPlateletTrend(LocalDateTime startTime, LocalDateTime endTime, String period);

    /**
     * 北京各区采血量统计
     */
    DistrictCollectionRespVO getDistrictCollection(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 采血机构排行（支持采血类型和组织模式多选聚合）
     */
    CenterRankingRespVO getCenterRanking(LocalDateTime startTime, LocalDateTime endTime,
            List<String> trendTypes, List<String> orgModes);

    /**
     * 献血类型排行（献血方舱、献血屋、献血车）
     */
    DonationTypeRankingRespVO getDonationTypeRanking(LocalDateTime startTime, LocalDateTime endTime,
            List<String> bloodTypes);

    /**
     * 献血点采血排名（关联 blood_collection_site.site_name_system 与 blood_collection_fact.collection_site）
     */
    CollectionSiteRankingRespVO getCollectionSiteRanking(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 每季度采血量统计（全血、单采血小板、采血人次）
     */
    QuarterlyCollectionRespVO getQuarterlySummary(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 首页统计概览（采血总量、供血总量、人次等汇总）
     */
    HomePageStatsRespVO getHomePageStats(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 北京血液中心信息及统计（坐标 + 供血/采血聚合量）
     */
    MainSiteStatsRespVO getMainSiteStats(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 血型分布统计（A/B/O/AB，支持按血液类型筛选：全血采血量、血小板单采量）
     */
    BloodTypeDistributionRespVO getBloodTypeDistribution(LocalDateTime startTime, LocalDateTime endTime,
            List<String> bloodTypes);

}
