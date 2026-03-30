package cn.iocoder.yudao.module.infra.controller.admin.blood;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodProductSupplyStatsRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.CollectionDistrictSupplyRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.CollectionOrgSupplyRankReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.CollectionOrgSupplyRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodUsagePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodUsageRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyStatsReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalSupplyStatsRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.DistrictHospitalRankReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.DistrictHospitalRankRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.HospitalBloodDistributionRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.TopHospitalBloodSourceReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.TopHospitalBloodSourceRespVO;
import cn.iocoder.yudao.module.infra.service.blood.BloodSupplyStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 供血统计")
@RestController
@RequestMapping("/infra/blood-supply-statistics")
@Validated
@PermitAll
@TenantIgnore
public class BloodSupplyStatisticsController {

    @Resource
    private BloodSupplyStatisticsService bloodSupplyStatisticsService;

    @Operation(summary = "医院信息列表（非部队医院），含总供血记录数")
    @GetMapping("/hospital-list")
    public CommonResult<List<HospitalSupplyStatsRespVO>> getHospitalList(@Valid HospitalSupplyStatsReqVO reqVO) {
        List<HospitalSupplyStatsRespVO> list = bloodSupplyStatisticsService.getHospitalSupplyStats(
                reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getDistricts());
        return success(list);
    }

    @Operation(summary = "医院被供血聚合统计（按时间周期+血液类型分组，支持行政区和血液类型多选过滤）")
    @GetMapping("/hospital-supply-agg")
    public CommonResult<List<HospitalSupplyAggRespVO>> getHospitalSupplyAgg(@Valid HospitalSupplyAggReqVO reqVO) {
        List<HospitalSupplyAggRespVO> list = bloodSupplyStatisticsService.getHospitalSupplyAgg(
                reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getDistricts(), reqVO.getBloodProductTypes(), reqVO.getPeriod());
        return success(list);
    }

    @Operation(summary = "机构供血量排行")
    @GetMapping("/collection-org-supply-rank")
    public CommonResult<List<CollectionOrgSupplyRankRespVO>> getCollectionOrgSupplyRank(
            @Valid CollectionOrgSupplyRankReqVO reqVO) {
        List<CollectionOrgSupplyRankRespVO> list = bloodSupplyStatisticsService.getCollectionOrgSupplyRank(
                reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getDistricts(), reqVO.getBloodProductTypes());
        return success(list);
    }

    @Operation(summary = "各区供血情况")
    @GetMapping("/collection-district-supply-rank")
    public CommonResult<CollectionDistrictSupplyRankRespVO> getCollectionDistrictSupplyRank(
            @Valid CollectionOrgSupplyRankReqVO reqVO) {
        CollectionDistrictSupplyRankRespVO result = bloodSupplyStatisticsService.getCollectionDistrictSupplyRank(
                reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getDistricts(), reqVO.getBloodProductTypes());
        return success(result);
    }

    @Operation(summary = "供血产品占比 && 供血量情况")
    @GetMapping("/blood-product-supply-stats")
    public CommonResult<List<BloodProductSupplyStatsRespVO>> getBloodProductSupplyStats(
            @Valid CollectionOrgSupplyRankReqVO reqVO) {
        List<BloodProductSupplyStatsRespVO> list = bloodSupplyStatisticsService.getBloodProductSupplyStats(
                reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getDistricts(), reqVO.getBloodProductTypes());
        return success(list);
    }

    @Operation(summary = "医疗机构发放排行")
    @GetMapping("/hospital-blood-usage-page")
    public CommonResult<PageResult<HospitalBloodUsageRespVO>> getHospitalBloodUsagePage(
            @Valid HospitalBloodUsagePageReqVO reqVO) {
        return success(bloodSupplyStatisticsService.getHospitalBloodUsagePage(reqVO));
    }

    @Operation(summary = "医疗机构发放详情")
    @GetMapping("/top-hospital-blood-source")
    public CommonResult<TopHospitalBloodSourceRespVO> getTopHospitalBloodSource(
            @Valid TopHospitalBloodSourceReqVO reqVO) {
        return success(bloodSupplyStatisticsService.getTopHospitalBloodSource(reqVO));
    }

    @Operation(summary = "各区医院发放排行")
    @GetMapping("/district-hospital-rank")
    public CommonResult<List<DistrictHospitalRankRespVO>> getDistrictHospitalRank(
            @Valid DistrictHospitalRankReqVO reqVO) {
        return success(bloodSupplyStatisticsService.getDistrictHospitalRank(reqVO));
    }

    @Operation(summary = "医院供血分布（环形图数据）。" +
            "默认按机构属性(公立/民营/其他)分组；" +
            "填 jiGouShuXing 则按机构类别(L2)细分；" +
            "填 yiYuanLeiXing 则按医院专业类型(L2, 20类)细分；" +
            "返回各分类的医院数量占比和供血量占比。")
    @GetMapping("/hospital-blood-distribution")
    public CommonResult<HospitalBloodDistributionRespVO> getHospitalBloodDistribution(
            @Valid HospitalBloodDistributionReqVO reqVO) {
        return success(bloodSupplyStatisticsService.getHospitalBloodDistribution(reqVO));
    }

}
