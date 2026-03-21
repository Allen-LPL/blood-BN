package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 献血点采血排名 Response VO")
@Data
public class CollectionSiteRankingRespVO {

    @Schema(description = "献血点采血排名列表")
    private List<Item> items;

    @Schema(description = "献血点采血排名数据项")
    @Data
    public static class Item {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "采血机构")
        private String collectionAgency;

        @Schema(description = "机构名（blood_collection_site.operating_org）")
        private String operatingOrg;

        @Schema(description = "献血点系统名称（blood_collection_site.site_name_system）")
        private String siteNameSystem;

        @Schema(description = "采血量总计（基础单位U，= 全血采血量 + 血小板单采量）")
        private BigDecimal totalUnit;

        @Schema(description = "全血采血量（基础单位U）")
        private BigDecimal wholeBloodUnit;

        @Schema(description = "血小板单采量（基础单位U）")
        private BigDecimal plateletUnit;

        @Schema(description = "采血人次")
        private Long count;

    }

}
