package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 每季度采血量统计 Response VO")
@Data
public class QuarterlyCollectionRespVO {

    @Schema(description = "季度采血量列表")
    private List<Item> items;

    @Schema(description = "季度采血量数据项")
    @Data
    public static class Item {

        @Schema(description = "季度标签", example = "2025-Q1")
        private String quarter;

        @Schema(description = "全血采血量（基础单位U）")
        private BigDecimal wholeBloodUnit;

        @Schema(description = "单采血小板采血量（基础单位U）")
        private BigDecimal plateletUnit;

        @Schema(description = "采血总人次")
        private Long totalCount;

    }

}
