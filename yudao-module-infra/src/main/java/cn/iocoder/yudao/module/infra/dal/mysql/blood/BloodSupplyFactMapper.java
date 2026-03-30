package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface BloodSupplyFactMapper extends BaseMapperX<BloodSupplyFactDO> {

    /**
     * 按 receivingOrg 聚合计数，支持时间范围和行政区过滤
     * 返回列：receiving_org, cnt
     */
    @Select("<script>" +
            "SELECT receiving_org, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='districts != null and districts.size() > 0'>" +
            "  AND receiving_org_admin_region IN " +
            "  <foreach item='item' collection='districts' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY receiving_org" +
            "</script>")
    List<Map<String, Object>> selectReceivingOrgCounts(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("districts") List<String> districts);

    default Map<String, Long> selectReceivingOrgCountMap(LocalDateTime startTime, LocalDateTime endTime,
                                                         List<String> districts) {
        List<Map<String, Object>> rows = selectReceivingOrgCounts(startTime, endTime, districts);
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String org = (String) row.get("receiving_org");
            Long cnt = ((Number) row.get("cnt")).longValue();
            if (org != null) {
                result.put(org, cnt);
            }
        }
        return result;
    }

    /**
     * 按 receivingOrg + 时间周期 + blood_product_name 聚合计数
     * 返回列：receiving_org, time_period, blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT receiving_org, " +
            "DATE_FORMAT(issue_time, #{periodFormat}) AS time_period, " +
            "blood_product_name, " +
            "COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='districts != null and districts.size() > 0'>" +
            "  AND receiving_org_admin_region IN " +
            "  <foreach item='item' collection='districts' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY receiving_org, time_period, blood_product_name " +
            "ORDER BY receiving_org, time_period" +
            "</script>")
    List<Map<String, Object>> selectOrgPeriodProductCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("districts") List<String> districts,
            @Param("periodFormat") String periodFormat);

    /**
     * 按 issuingOrg + blood_product_name 聚合计数，支持时间范围和行政区过滤
     * 返回列：issuing_org, blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT issuing_org, blood_product_name, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='districts != null and districts.size() > 0'>" +
            "  AND receiving_org_admin_region IN " +
            "  <foreach item='item' collection='districts' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY issuing_org, blood_product_name " +
            "ORDER BY issuing_org" +
            "</script>")
    List<Map<String, Object>> selectIssuingOrgProductCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("districts") List<String> districts);

    /**
     * 按 issuingOrg + receivingOrgAdminRegion + blood_product_name 聚合计数，支持时间范围和行政区过滤
     * 返回列：issuing_org, receiving_org_admin_region, blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT issuing_org, receiving_org_admin_region, blood_product_name, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='districts != null and districts.size() > 0'>" +
            "  AND receiving_org_admin_region IN " +
            "  <foreach item='item' collection='districts' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY issuing_org, receiving_org_admin_region, blood_product_name " +
            "ORDER BY issuing_org, receiving_org_admin_region" +
            "</script>")
    List<Map<String, Object>> selectIssuingOrgDistrictProductCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("districts") List<String> districts);

    /**
     * 按 receivingOrg + blood_product_name 聚合计数，按指定机构名称列表过滤
     * 返回列：receiving_org, blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT receiving_org, blood_product_name, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='receivingOrgs != null and receivingOrgs.size() > 0'>" +
            "  AND receiving_org IN " +
            "  <foreach item='item' collection='receivingOrgs' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY receiving_org, blood_product_name" +
            "</script>")
    List<Map<String, Object>> selectReceivingOrgProductCountsByOrgs(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("receivingOrgs") List<String> receivingOrgs);

    /**
     * 按 blood_product_name 聚合计数，支持时间范围和行政区过滤，按数量降序
     * 返回列：blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT blood_product_name, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "<if test='districts != null and districts.size() > 0'>" +
            "  AND receiving_org_admin_region IN " +
            "  <foreach item='item' collection='districts' open='(' separator=',' close=')'>#{item}</foreach>" +
            "</if>" +
            "GROUP BY blood_product_name " +
            "ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> selectProductNameCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("districts") List<String> districts);

    /**
     * 对指定受血机构，按 issuingOrg + blood_product_name 聚合计数，支持时间范围过滤
     * 返回列：issuing_org, blood_product_name, cnt
     */
    @Select("<script>" +
            "SELECT issuing_org, blood_product_name, COUNT(*) AS cnt " +
            "FROM blood_supply_fact " +
            "WHERE receiving_org = #{receivingOrg} " +
            "<if test='startTime != null'> AND issue_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND issue_time &lt;= #{endTime} </if>" +
            "GROUP BY issuing_org, blood_product_name" +
            "</script>")
    List<Map<String, Object>> selectIssuingOrgProductCountsByReceivingOrg(
            @Param("receivingOrg") String receivingOrg,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

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
