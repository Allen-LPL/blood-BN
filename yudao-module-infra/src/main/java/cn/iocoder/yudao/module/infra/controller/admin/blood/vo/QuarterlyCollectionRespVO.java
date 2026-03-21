package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 各区采血量统计 Response VO")
@Data
public class QuarterlyCollectionRespVO {

    @Schema(description = "各区采血量列表")
    private List<Item> items;

    @Schema(description = "各区采血量数据项")
    @Data
    public static class Item {

        @Schema(description = "行政区域", example = "城区-海淀区")
        private String district;

        @Schema(description = "全血采血量（基础单位U）")
        private BigDecimal wholeBloodUnit;

        @Schema(description = "单采血小板采血量（基础单位U）")
        private BigDecimal plateletUnit;

        // 采血总量（基础单位U）
        @Schema(description = "采血总量（基础单位U）")
        private BigDecimal totalUnit;

        @Schema(description = "采血总人次")
        private Long totalCount;

    }

}
