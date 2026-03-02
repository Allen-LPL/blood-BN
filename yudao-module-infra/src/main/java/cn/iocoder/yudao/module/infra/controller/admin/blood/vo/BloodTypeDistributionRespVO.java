package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 血型占比统计 Response VO")
@Data
public class BloodTypeDistributionRespVO {

    @Schema(description = "统计项列表")
    private List<Item> items;

    @Schema(description = "总人次")
    private Long totalCount;

    @Data
    public static class Item {

        @Schema(description = "血型", example = "A型")
        private String bloodType;

        @Schema(description = "人次")
        private Long count;

        @Schema(description = "占比（0~1）", example = "0.2500")
        private BigDecimal ratio;
    }

}
