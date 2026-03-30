package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "医院被供血聚合统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSupplyAggRespVO {

    @Schema(description = "医院全称")
    private String yiYuanQuanCheng;

    @Schema(description = "机构属性（公立/民营/其他）")
    private String jiGouShuXing;

    @Schema(description = "按时间周期分组的供血数据")
    private List<PeriodItem> periods;

    @Schema(description = "时间周期供血明细")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodItem {

        @Schema(description = "时间周期标签", example = "2025-01")
        private String period;

        @Schema(description = "红细胞类供血记录数")
        private Long redBloodCellCount;

        @Schema(description = "血小板类供血记录数")
        private Long plateletCount;

        @Schema(description = "血浆类供血记录数")
        private Long plasmaCount;

        @Schema(description = "合计供血记录数")
        private Long totalCount;

    }

}
