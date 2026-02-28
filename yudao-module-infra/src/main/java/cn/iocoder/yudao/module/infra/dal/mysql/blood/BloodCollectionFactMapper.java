package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BloodCollectionFactMapper extends BaseMapperX<BloodCollectionFactDO> {

    default PageResult<BloodCollectionFactDO> selectPage(BloodCollectionFactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BloodCollectionFactDO>()
                .eqIfPresent(BloodCollectionFactDO::getArchiveId, reqVO.getArchiveId())
                .eqIfPresent(BloodCollectionFactDO::getDonationCode, reqVO.getDonationCode())
                .eqIfPresent(BloodCollectionFactDO::getLoadBatchId, reqVO.getLoadBatchId())
                .eqIfPresent(BloodCollectionFactDO::getCollectionDepartment, reqVO.getCollectionDepartment())
                .eqIfPresent(BloodCollectionFactDO::getCollectionSite, reqVO.getCollectionSite())
                .eqIfPresent(BloodCollectionFactDO::getOrganizationMode, reqVO.getOrganizationMode())
                .eqIfPresent(BloodCollectionFactDO::getDonationType, reqVO.getDonationType())
                .eqIfPresent(BloodCollectionFactDO::getGender, reqVO.getGender())
                .eqIfPresent(BloodCollectionFactDO::getFullVolumeFlag, reqVO.getFullVolumeFlag())
                .eqIfPresent(BloodCollectionFactDO::getArchiveBloodType, reqVO.getArchiveBloodType())
                .eqIfPresent(BloodCollectionFactDO::getPrecheckBloodType, reqVO.getPrecheckBloodType())
                .eqIfPresent(BloodCollectionFactDO::getUnitAdminRegion, reqVO.getUnitAdminRegion())
                .likeIfPresent(BloodCollectionFactDO::getOrgUnitName, reqVO.getOrgUnitName())
                .likeIfPresent(BloodCollectionFactDO::getSystemName, reqVO.getSystemName())
                .likeIfPresent(BloodCollectionFactDO::getParentUnit, reqVO.getParentUnit())
                .betweenIfPresent(BloodCollectionFactDO::getRegistrationTime, reqVO.getRegistrationTime())
                .betweenIfPresent(BloodCollectionFactDO::getPrecheckTime, reqVO.getPrecheckTime())
                .betweenIfPresent(BloodCollectionFactDO::getBloodCollectionTime, reqVO.getBloodCollectionTime())
                .betweenIfPresent(BloodCollectionFactDO::getIngestedAt, reqVO.getIngestedAt())
                .geIfPresent(BloodCollectionFactDO::getAge, reqVO.getAgeMin())
                .leIfPresent(BloodCollectionFactDO::getAge, reqVO.getAgeMax())
                .and(reqVO.getKeyword() != null && !reqVO.getKeyword().trim().isEmpty(), w -> w
                        .eq(BloodCollectionFactDO::getDonationCode, reqVO.getKeyword())
                        .or()
                        .eq(BloodCollectionFactDO::getArchiveId, reqVO.getKeyword()))
                .orderByDesc(BloodCollectionFactDO::getBloodCollectionTime)
                .orderByDesc(BloodCollectionFactDO::getId));
    }

}
