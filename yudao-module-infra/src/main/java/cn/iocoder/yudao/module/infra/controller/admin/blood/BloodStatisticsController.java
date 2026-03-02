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
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
    @Operation(summary = "全血采血量趋势（按日/周/月/年）")
    public CommonResult<CollectionTrendRespVO> getWholeBloodTrend(@Valid BloodStatisticsTrendReqVO reqVO) {
        return success(bloodStatisticsService.getWholeBloodTrend(
                reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getPeriod()));
    }

    @GetMapping("/platelet-trend")
    @Operation(summary = "单采血小板采血量趋势（按日/周/月/年）")
    public CommonResult<CollectionTrendRespVO> getPlateletTrend(@Valid BloodStatisticsTrendReqVO reqVO) {
        return success(bloodStatisticsService.getPlateletTrend(
                reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getPeriod()));
    }

    @GetMapping("/district-collection")
    @Operation(summary = "北京各区采血量统计")
    public CommonResult<DistrictCollectionRespVO> getDistrictCollection(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getDistrictCollection(reqVO.getStartTime(), reqVO.getEndTime()));
    }

    @GetMapping("/center-ranking")
    @Operation(summary = "各血液中心采血量排名")
    public CommonResult<CenterRankingRespVO> getCenterRanking(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getCenterRanking(reqVO.getStartTime(), reqVO.getEndTime()));
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
    @Operation(summary = "采血表血型占比统计（按 precheck_blood_type 字段）")
    public CommonResult<BloodTypeDistributionRespVO> getBloodTypeDistribution(@Valid BloodStatisticsReqVO reqVO) {
        return success(bloodStatisticsService.getBloodTypeDistribution(reqVO.getStartTime(), reqVO.getEndTime()));
    }

}
