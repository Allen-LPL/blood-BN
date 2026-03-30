package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "医院供血分布统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalBloodDistributionRespVO {

    @Schema(description = "统计维度说明（如：按机构属性 / 按机构类别 / 按医院类型 / 按医院专业类型）")
    private String groupByDimension;

    @Schema(description = "符合筛选条件的医院总数")
    private Long totalHospitalCount;

    @Schema(description = "供血总量（合计）")
    private Long totalBloodCount;

    @Schema(description = "红细胞类供血总量")
    private Long totalRedBloodCellCount;

    @Schema(description = "血小板类供血总量")
    private Long totalPlateletCount;

    @Schema(description = "血浆类供血总量")
    private Long totalPlasmaCount;

    @Schema(description = "各分类的分布明细，按供血总量倒序")
    private List<DistributionItem> items;

    @Schema(description = "分布明细条目")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionItem {

        @Schema(description = "分类名称")
        private String category;

        @Schema(description = "该分类医院数量")
        private Long hospitalCount;

        @Schema(description = "医院数量占比（%，保留2位小数）")
        private Double hospitalRatio;

        @Schema(description = "红细胞类供血量")
        private Long redBloodCellCount;

        @Schema(description = "血小板类供血量")
        private Long plateletCount;

        @Schema(description = "血浆类供血量")
        private Long plasmaCount;

        @Schema(description = "合计供血量")
        private Long bloodCount;

        @Schema(description = "供血量占比（%，保留2位小数）")
        private Double bloodRatio;

    }

}
