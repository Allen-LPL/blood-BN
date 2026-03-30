package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "采血机构行政区供血量排名 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDistrictSupplyRankRespVO {

    @Schema(description = "各行政区供血聚合数据倒序排名")
    private List<DistrictRankItem> districtRanks;

    @Schema(description = "各采血站点给北京市各行政区分别供血的聚合总量")
    private List<StationDistrictSupplyItem> stationDistrictSupplies;

    @Schema(description = "各采血站点给北京市各行政区供血的百分比占量")
    private List<StationDistrictPercentItem> stationDistrictPercents;

    @Schema(description = "行政区供血量排名条目")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistrictRankItem {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "行政区")
        private String district;

        @Schema(description = "红细胞类供血记录数")
        private Long redBloodCellCount;

        @Schema(description = "血小板类供血记录数")
        private Long plateletCount;

        @Schema(description = "血浆类供血记录数")
        private Long plasmaCount;

        @Schema(description = "合计供血记录数")
        private Long totalCount;

    }

    @Schema(description = "采血站点给各行政区供血聚合总量")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationDistrictSupplyItem {

        @Schema(description = "采血机构名称")
        private String issuingOrg;

        @Schema(description = "该站点给各行政区的供血量明细")
        private List<DistrictSupply> districtSupplies;

        @Schema(description = "行政区供血量明细")
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DistrictSupply {

            @Schema(description = "行政区")
            private String district;

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

    @Schema(description = "采血站点给各行政区供血百分比")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationDistrictPercentItem {

        @Schema(description = "采血机构名称")
        private String issuingOrg;

        @Schema(description = "该站点对各行政区的供血占比明细")
        private List<DistrictPercent> districtPercents;

        @Schema(description = "行政区供血占比明细")
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DistrictPercent {

            @Schema(description = "行政区")
            private String district;

            @Schema(description = "该站点给该行政区的供血量")
            private Long stationSupplyCount;

            @Schema(description = "该行政区的总供血量（含所有采血机构）")
            private Long districtTotalCount;

            @Schema(description = "该站点在该行政区的供血占比（%，保留2位小数）：stationSupplyCount / districtTotalCount", example = "10.00")
            private Double districtPercentage;

            @Schema(description = "该站点的总供血量（对所有行政区合计）")
            private Long stationTotalCount;

            @Schema(description = "该站点供给该行政区的占比（%，保留2位小数）：stationSupplyCount / stationTotalCount", example = "25.00")
            private Double stationOutputPercentage;

        }

    }

}
