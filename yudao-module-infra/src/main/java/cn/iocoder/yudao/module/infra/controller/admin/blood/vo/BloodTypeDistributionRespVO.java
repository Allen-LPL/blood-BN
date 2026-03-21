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

    @Schema(description = "全血总人次")
    private Long wholeBloodTotalCount;

    @Schema(description = "血小板总人次")
    private Long plateletTotalCount;

    @Schema(description = "全血总采血量（基础单位U）")
    private BigDecimal wholeBloodTotalUnit;

    @Schema(description = "血小板总采血量（基础单位U）")
    private BigDecimal plateletTotalUnit;

    @Data
    public static class Item {

        @Schema(description = "血型", example = "A型")
        private String bloodType;

        @Schema(description = "人次")
        private Long count;

        @Schema(description = "占比（0~1）", example = "0.2500")
        private BigDecimal ratio;

        @Schema(description = "全血人次")
        private Long wholeBloodCount;

        @Schema(description = "全血血型占比（占全血总人次，0~1）", example = "0.2500")
        private BigDecimal wholeBloodRatio;

        @Schema(description = "全血采血量（基础单位U）")
        private BigDecimal wholeBloodUnit;

        @Schema(description = "血小板人次")
        private Long plateletCount;

        @Schema(description = "血小板血型占比（占血小板总人次，0~1）", example = "0.2500")
        private BigDecimal plateletRatio;

        @Schema(description = "血小板采血量（基础单位U）")
        private BigDecimal plateletUnit;
    }

}
