package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 采血量趋势统计 Response VO")
@Data
public class CollectionTrendRespVO {

    @Schema(description = "趋势数据列表")
    private List<Item> items;

    @Schema(description = "趋势数据项")
    @Data
    public static class Item {

        @Schema(description = "时间周期标签", example = "2025-01")
        private String period;

        @Schema(description = "采血量（基础单位U）")
        private BigDecimal totalUnit;

        @Schema(description = "采血人次")
        private Long count;

    }

}
