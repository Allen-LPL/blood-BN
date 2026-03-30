package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "各区医院用血量排行 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictHospitalRankRespVO {

    @Schema(description = "行政区排名（按各行政区所有医院用血总量倒序）")
    private Integer rank;

    @Schema(description = "行政区名称")
    private String district;

    @Schema(description = "该行政区所有医院红细胞类用血总量")
    private Long districtRedBloodCellCount;

    @Schema(description = "该行政区所有医院血小板类用血总量")
    private Long districtPlateletCount;

    @Schema(description = "该行政区所有医院血浆类用血总量")
    private Long districtPlasmaCount;

    @Schema(description = "该行政区所有医院合计用血总量")
    private Long districtTotalCount;

    @Schema(description = "该行政区各医院用血量排行（按总量倒序）")
    private List<HospitalItem> hospitals;

    @Schema(description = "医院用血量排行条目")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalItem {

        @Schema(description = "在该行政区内的排名")
        private Integer rank;

        @Schema(description = "医院全称")
        private String hospitalName;

        @Schema(description = "红细胞类用血量")
        private Long redBloodCellCount;

        @Schema(description = "血小板类用血量")
        private Long plateletCount;

        @Schema(description = "血浆类用血量")
        private Long plasmaCount;

        @Schema(description = "合计用血量")
        private Long totalCount;

    }

}
