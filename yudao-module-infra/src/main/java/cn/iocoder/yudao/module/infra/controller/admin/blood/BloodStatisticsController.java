package cn.iocoder.yudao.module.infra.controller.admin.blood;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.*;
import cn.iocoder.yudao.module.infra.service.blood.BloodStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import java.util.List;

@Tag(name = "管理后台 - 采供血统计")
@RestController
@RequestMapping("/infra/blood-statistics")
@Validated
@PermitAll
@TenantIgnore
public class BloodStatisticsController {

    @Resource
    private BloodStatisticsService bloodStatisticsService;

    @GetMapping("/donor-distribution")
    @Operation(summary = "献血者分布统计（全血、单采血小板、男性、女性）")
    public CommonResult<DonorDistributionRespVO> getDonorDistribution(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getDonorDistribution(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/whole-blood-trend")
    @Operation(summary = "采血趋势统计（支持多选：自愿献血、团体无偿、全血采血量、血小板单采量）")
    public CommonResult<CollectionTrendRespVO> getWholeBloodTrend(@Valid BloodStatisticsTrendReqVO reqVO) {
        return success(bloodStatisticsService.getWholeBloodTrend(
                reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getPeriod(), reqVO.getTrendTypes()));
    }

    @GetMapping("/platelet-trend")
    @Operation(summary = "采血量情况（按日/周/月/年）")
    public CommonResult<CollectionTrendRespVO> getPlateletTrend(@Valid BloodStatisticsTrendReqVO reqVO) {
        return success(bloodStatisticsService.getPlateletTrend(
                reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getPeriod()));
    }

    @GetMapping("/district-collection")
    @Operation(summary = "献血区域排行")
    public CommonResult<DistrictCollectionRespVO> getDistrictCollection(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getDistrictCollection(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/center-ranking")
    @Operation(summary = "采血机构排行（支持采血类型和组织模式多选聚合）")
    public CommonResult<CenterRankingRespVO> getCenterRanking(
            @Valid BloodStatisticsReqVO reqVO,
            @RequestParam(required = false) List<String> bloodVolumeTypes,
            @RequestParam(required = false) List<String> orgVolumeTypes) {
        return success(bloodStatisticsService.getCenterRanking(
                reqVO.getStartTime(), reqVO.getEndTime(), bloodVolumeTypes, orgVolumeTypes));
    }

    @GetMapping("/donation-type-ranking")
    @Operation(summary = "献血类型排行（献血方舱、献血屋、献血车）")
    public CommonResult<DonationTypeRankingRespVO> getDonationTypeRanking(
            @Valid BloodStatisticsReqVO reqVO,
            @RequestParam(required = false) List<String> bloodTypes) {
        return success(bloodStatisticsService.getDonationTypeRanking(
                reqVO.getStartTime(), reqVO.getEndTime(), bloodTypes));
    }

    @GetMapping("/collection-site-ranking")
    @Operation(summary = "献血点采血排名（关联 blood_collection_site.site_name_system 与 blood_collection_fact.collection_site）")
    public CommonResult<CollectionSiteRankingRespVO> getCollectionSiteRanking(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getCollectionSiteRanking(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/quarterly-summary")
    @Operation(summary = "每季度采血量统计（全血、单采血小板、采血人次）")
    public CommonResult<QuarterlyCollectionRespVO> getQuarterlySummary(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getQuarterlySummary(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/home-stats")
    @Operation(summary = "首页统计概览（采血总量、供血总量、人次等汇总）")
    public CommonResult<HomePageStatsRespVO> getHomePageStats(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getHomePageStats(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/main-site-stats")
    @Operation(summary = "北京血液中心信息及统计（坐标 + 供血/采血聚合量）")
    public CommonResult<MainSiteStatsRespVO> getMainSiteStats(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getMainSiteStats(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/blood-type-distribution")
    @Operation(summary = "血型分布统计（A/B/O/AB，支持按血液类型筛选：全血采血量、血小板单采量）")
    public CommonResult<BloodTypeDistributionRespVO> getBloodTypeDistribution(
            @Valid BloodStatisticsReqVO reqVO,
            @RequestParam(required = false) List<String> bloodTypes) {
        return success(bloodStatisticsService.getBloodTypeDistribution(
                reqVO.getStartTime(), reqVO.getEndTime(), bloodTypes));
    }

}
