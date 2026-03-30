package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "医院供血统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSupplyStatsRespVO {

    @Schema(description = "医院全称")
    private String yiYuanQuanCheng;

    @Schema(description = "机构属性（公立/民营/其他）")
    private String jiGouShuXing;

    @Schema(description = "被供血的记录数")
    private Long supplyCount;

}
