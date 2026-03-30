package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "血液品种供血量统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodProductSupplyStatsRespVO {

    @Schema(description = "血液品种名称")
    private String bloodProductName;

    @Schema(description = "品种分类（红细胞类、血小板类、血浆类、其他）")
    private String category;

    @Schema(description = "供血记录数")
    private Long supplyCount;

}
