package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 北京血液中心信息及统计 Response VO")
@Data
public class MainSiteStatsRespVO {

    @Schema(description = "血液中心列表")
    private List<Item> items;

    @Schema(description = "血液中心数据项")
    @Data
    public static class Item {

        @Schema(description = "主键")
        private Long id;

        @Schema(description = "运行机构名称")
        private String collectionOperatingOrg;

        @Schema(description = "类型 1:一级、2:二级")
        private Integer type;

        @Schema(description = "运行状态 0:关闭，1:正常运行")
        private Integer state;

        @Schema(description = "经度")
        private String coordinateLng;

        @Schema(description = "纬度")
        private String coordinateLat;

        @Schema(description = "供血量统计（基础单位U）- 来自 blood_supply_fact 的 issuing_org 聚合")
        private BigDecimal supplyTotalUnit;

        @Schema(description = "采血量统计（基础单位U）- 来自 blood_collection_fact 的 collection_department 聚合")
        private BigDecimal collectionTotalUnit;

    }

}
