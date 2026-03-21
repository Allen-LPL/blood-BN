package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BloodCollectionFactMapper extends BaseMapperX<BloodCollectionFactDO> {

        default PageResult<BloodCollectionFactDO> selectPage(BloodCollectionFactPageReqVO reqVO) {
                return selectPage(reqVO, new LambdaQueryWrapperX<BloodCollectionFactDO>()
                                .eqIfPresent(BloodCollectionFactDO::getArchiveId, reqVO.getArchiveId())
                                .eqIfPresent(BloodCollectionFactDO::getDonationCode, reqVO.getDonationCode())
                                .eqIfPresent(BloodCollectionFactDO::getLoadBatchId, reqVO.getLoadBatchId())
                                .eqIfPresent(BloodCollectionFactDO::getCollectionDepartment,
                                                reqVO.getCollectionDepartment())
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
                                .betweenIfPresent(BloodCollectionFactDO::getRegistrationTime,
                                                reqVO.getRegistrationTime())
                                .betweenIfPresent(BloodCollectionFactDO::getPrecheckTime, reqVO.getPrecheckTime())
                                .betweenIfPresent(BloodCollectionFactDO::getBloodCollectionTime,
                                                reqVO.getBloodCollectionTime())
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

        /**
         * 兼容 QueryWrapperX 的 Map 聚合查询。
         * <p>
         * 业务层直接调用 selectMaps(queryWrapperX) 时，统一转为 MyBatis-Plus 原生 Wrapper 执行。
         */
        default List<Map<String, Object>> selectMaps(QueryWrapperX<BloodCollectionFactDO> queryWrapper) {
                return this.selectMaps((Wrapper<BloodCollectionFactDO>) queryWrapper);
        }

        @Select("SELECT MIN(f.collection_department) AS collectionAgency, " +
                        "MIN(s.operating_org) AS operatingOrg, " +
                        "f.collection_site AS siteNameSystem, " +
                        "SUM(CASE WHEN f.blood_volume IS NOT NULL " +
                        "AND CAST(f.blood_volume AS DECIMAL(10,2)) >= 50 THEN f.base_unit_value ELSE 0 END) + " +
                        "SUM(CASE WHEN f.blood_volume IS NOT NULL " +
                        "AND CAST(f.blood_volume AS DECIMAL(10,2)) < 50 THEN f.base_unit_value ELSE 0 END) AS totalUnit, "
                        +
                        "SUM(CASE WHEN f.blood_volume IS NOT NULL " +
                        "AND CAST(f.blood_volume AS DECIMAL(10,2)) >= 50 THEN f.base_unit_value ELSE 0 END) AS wholeBloodUnit, "
                        +
                        "SUM(CASE WHEN f.blood_volume IS NOT NULL " +
                        "AND CAST(f.blood_volume AS DECIMAL(10,2)) < 50 THEN f.base_unit_value ELSE 0 END) AS plateletUnit, "
                        +
                        "COUNT(*) AS cnt " +
                        "FROM blood_collection_fact f " +
                        "INNER JOIN blood_collection_site s ON s.site_name_system = f.collection_site " +
                        "WHERE f.blood_collection_time BETWEEN #{startTime} AND #{endTime} " +
                        "AND f.collection_site IS NOT NULL " +
                        "AND s.site_name_system IS NOT NULL " +
                        "GROUP BY f.collection_site " +
                        "ORDER BY totalUnit DESC")
        List<Map<String, Object>> selectCollectionSiteRanking(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

}
