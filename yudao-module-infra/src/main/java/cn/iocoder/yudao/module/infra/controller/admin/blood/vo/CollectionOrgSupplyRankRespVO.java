package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "采血机构供血量排名 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionOrgSupplyRankRespVO {

    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "采血机构名称")
    private String issuingOrg;

    @Schema(description = "红细胞类供血记录数")
    private Long redBloodCellCount;

    @Schema(description = "血小板类供血记录数")
    private Long plateletCount;

    @Schema(description = "血浆类供血记录数")
    private Long plasmaCount;

    @Schema(description = "合计供血记录数")
    private Long totalCount;

}
