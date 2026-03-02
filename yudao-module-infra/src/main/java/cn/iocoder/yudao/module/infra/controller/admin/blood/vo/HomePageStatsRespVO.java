package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 采供血首页统计概览 Response VO")
@Data
public class HomePageStatsRespVO {

    // ==================== 采血概览 ====================

    @Schema(description = "采血总人次")
    private Long collectionTotalCount;

    @Schema(description = "采血总量（基础单位U）")
    private BigDecimal collectionTotalUnit;

    @Schema(description = "全血采血量（基础单位U）")
    private BigDecimal wholeBloodUnit;

    @Schema(description = "单采血小板采血量（基础单位U）")
    private BigDecimal plateletUnit;

    // ==================== 供血概览 ====================

    @Schema(description = "供血总记录数")
    private Long supplyTotalCount;

    @Schema(description = "供血总量（基础单位U）")
    private BigDecimal supplyTotalUnit;

    // ==================== 献血者概览 ====================

    @Schema(description = "男性献血人次")
    private Long maleCount;

    @Schema(description = "女性献血人次")
    private Long femaleCount;

    // ==================== 机构概览 ====================

    @Schema(description = "运行中的血液中心数量")
    private Long activeSiteCount;

}
