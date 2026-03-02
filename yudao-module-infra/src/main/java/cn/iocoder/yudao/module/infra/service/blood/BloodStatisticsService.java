package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*;

import java.time.LocalDateTime;

public interface BloodStatisticsService {

    /**
     * 献血者分布统计（全血、单采血小板、男性、女性）
     */
    DonorDistributionRespVO getDonorDistribution(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 全血采血量趋势（按日/周/月/年）
     */
    CollectionTrendRespVO getWholeBloodTrend(LocalDateTime startTime, LocalDateTime endTime, String period);

    /**
     * 单采血小板采血量趋势（按日/周/月/年）
     */
    CollectionTrendRespVO getPlateletTrend(LocalDateTime startTime, LocalDateTime endTime, String period);

    /**
     * 北京各区采血量统计
     */
    DistrictCollectionRespVO getDistrictCollection(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 各血液中心采血量排名
     */
    CenterRankingRespVO getCenterRanking(LocalDateTime startTime, LocalDateTime endTime);

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
     * 采血表血型占比统计（按 precheck_blood_type 字段）
     */
    BloodTypeDistributionRespVO getBloodTypeDistribution(LocalDateTime startTime, LocalDateTime endTime);

}
