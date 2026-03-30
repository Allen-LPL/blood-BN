package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "用血量倒排第一医院供血来源统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopHospitalBloodSourceRespVO {

    @Schema(description = "用血量最高的医院名称")
    private String topHospitalName;

    @Schema(description = "该医院总用血量")
    private Long topHospitalTotalCount;

    @Schema(description = "各采供血机构的供血量统计，按总量倒序排名")
    private List<SourceItem> sources;

    @Schema(description = "采供血机构供血量条目")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceItem {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "采供血机构名称")
        private String issuingOrg;

        @Schema(description = "红细胞类供血量")
        private Long redBloodCellCount;

        @Schema(description = "血小板类供血量")
        private Long plateletCount;

        @Schema(description = "血浆类供血量")
        private Long plasmaCount;

        @Schema(description = "合计供血量")
        private Long totalCount;

    }

}
