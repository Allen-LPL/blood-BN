package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "医疗机构用血量统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalBloodUsageRespVO {

    @Schema(description = "医院全称")
    private String yiYuanQuanCheng;

    @Schema(description = "行政区")
    private String xingZhengQuYu;

    @Schema(description = "机构属性（公立/民营/其他）")
    private String jiGouShuXing;

    @Schema(description = "机构类别")
    private String jiGouLeiBie;

    @Schema(description = "医院类型")
    private String yiYuanLeiXing;

    @Schema(description = "红细胞类用血量")
    private Long redBloodCellCount;

    @Schema(description = "血小板类用血量")
    private Long plateletCount;

    @Schema(description = "血浆类用血量")
    private Long plasmaCount;

    @Schema(description = "合计用血量")
    private Long totalCount;

}
