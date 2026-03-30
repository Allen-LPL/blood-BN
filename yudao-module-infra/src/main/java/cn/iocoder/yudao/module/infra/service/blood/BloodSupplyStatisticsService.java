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

import java.time.LocalDateTime;
import java.util.List;

public interface BloodSupplyStatisticsService {

    List<HospitalSupplyStatsRespVO> getHospitalSupplyStats(LocalDateTime startTime, LocalDateTime endTime,
                                                           List<String> districts);

    List<HospitalSupplyAggRespVO> getHospitalSupplyAgg(LocalDateTime startTime, LocalDateTime endTime,
                                                        List<String> districts, List<String> bloodProductTypes,
                                                        String period);

    List<CollectionOrgSupplyRankRespVO> getCollectionOrgSupplyRank(LocalDateTime startTime, LocalDateTime endTime,
                                                                    List<String> districts,
                                                                    List<String> bloodProductTypes);

    CollectionDistrictSupplyRankRespVO getCollectionDistrictSupplyRank(LocalDateTime startTime, LocalDateTime endTime,
                                                                        List<String> districts,
                                                                        List<String> bloodProductTypes);

    List<BloodProductSupplyStatsRespVO> getBloodProductSupplyStats(LocalDateTime startTime, LocalDateTime endTime,
                                                                    List<String> districts,
                                                                    List<String> bloodProductTypes);

    PageResult<HospitalBloodUsageRespVO> getHospitalBloodUsagePage(HospitalBloodUsagePageReqVO reqVO);

    TopHospitalBloodSourceRespVO getTopHospitalBloodSource(TopHospitalBloodSourceReqVO reqVO);

    List<DistrictHospitalRankRespVO> getDistrictHospitalRank(DistrictHospitalRankReqVO reqVO);

    HospitalBloodDistributionRespVO getHospitalBloodDistribution(HospitalBloodDistributionReqVO reqVO);

}
