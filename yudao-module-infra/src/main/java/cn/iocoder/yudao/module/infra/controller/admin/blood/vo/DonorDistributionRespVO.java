package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 献血者分布统计 Response VO")
@Data
public class DonorDistributionRespVO {

    @Schema(description = "总人次")
    private Long totalCount;

    @Schema(description = "全血人次")
    private Long wholeBloodCount;

    @Schema(description = "单采血小板人次")
    private Long plateletCount;

    @Schema(description = "男性人次")
    private Long maleCount;

    @Schema(description = "女性人次")
    private Long femaleCount;

    @Schema(description = "全血占比")
    private BigDecimal wholeBloodRatio;

    @Schema(description = "单采血小板占比")
    private BigDecimal plateletRatio;

    @Schema(description = "男性占比")
    private BigDecimal maleRatio;

    @Schema(description = "女性占比")
    private BigDecimal femaleRatio;

    @Schema(description = "年龄分布列表")
    private List<AgeDistributionItem> ageDistribution;

    @Schema(description = "年龄分布数据项")
    @Data
    public static class AgeDistributionItem {

        @Schema(description = "年龄段标签", example = "18岁-25岁")
        private String ageGroup;

        @Schema(description = "人次")
        private Long count;

        @Schema(description = "占比")
        private BigDecimal ratio;
    }

}
