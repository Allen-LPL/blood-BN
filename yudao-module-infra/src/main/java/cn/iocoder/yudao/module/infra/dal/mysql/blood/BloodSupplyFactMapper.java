package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BloodSupplyFactMapper extends BaseMapperX<BloodSupplyFactDO> {

    default PageResult<BloodSupplyFactDO> selectPage(BloodSupplyFactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BloodSupplyFactDO>()
                .eqIfPresent(BloodSupplyFactDO::getDonationCode, reqVO.getDonationCode())
                .eqIfPresent(BloodSupplyFactDO::getProductCode, reqVO.getProductCode())
                .eqIfPresent(BloodSupplyFactDO::getLoadBatchId, reqVO.getLoadBatchId())
                .eqIfPresent(BloodSupplyFactDO::getAbo, reqVO.getAbo())
                .eqIfPresent(BloodSupplyFactDO::getRhd, reqVO.getRhd())
                .eqIfPresent(BloodSupplyFactDO::getBloodProductName, reqVO.getBloodProductName())
                .eqIfPresent(BloodSupplyFactDO::getIssueType, reqVO.getIssueType())
                .eqIfPresent(BloodSupplyFactDO::getIssuingOrg, reqVO.getIssuingOrg())
                .eqIfPresent(BloodSupplyFactDO::getReceivingOrg, reqVO.getReceivingOrg())
                .eqIfPresent(BloodSupplyFactDO::getReceivingOrgAdminRegion, reqVO.getReceivingOrgAdminRegion())
                .betweenIfPresent(BloodSupplyFactDO::getIssueTime, reqVO.getIssueTime())
                .betweenIfPresent(BloodSupplyFactDO::getBloodExpiryTime, reqVO.getBloodExpiryTime())
                .betweenIfPresent(BloodSupplyFactDO::getIngestedAt, reqVO.getIngestedAt())
                .and(reqVO.getKeyword() != null && !reqVO.getKeyword().trim().isEmpty(), w -> w
                        .eq(BloodSupplyFactDO::getDonationCode, reqVO.getKeyword())
                        .or()
                        .eq(BloodSupplyFactDO::getProductCode, reqVO.getKeyword()))
                .orderByDesc(BloodSupplyFactDO::getIssueTime)
                .orderByDesc(BloodSupplyFactDO::getId));
    }

}
