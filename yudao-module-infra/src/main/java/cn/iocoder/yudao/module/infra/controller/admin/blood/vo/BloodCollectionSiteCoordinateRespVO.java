package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 采血点坐标数据 Response VO")
@Data
public class BloodCollectionSiteCoordinateRespVO {

    @Schema(description = "采血点列表")
    private List<Item> items;

    @Schema(description = "血液中心列表")
    private List<MainItem> mainItems;

    @Schema(description = "采血点数据项")
    @Data
    public static class Item {

        @Schema(description = "献血点系统名称")
        private String siteNameSystem;

        @Schema(description = "采血点名称")
        private String collectionSiteName;

        @Schema(description = "献血点名称（备案）")
        private String siteNameFiling;

        @Schema(description = "日常开放时间")
        private String dailyOpenTime;

        @Schema(description = "地址")
        private String specificAddress;

        @Schema(description = "所属区")
        private String district;

        @Schema(description = "运行机构")
        private String operatingOrg;

        @Schema(description = "经度")
        private BigDecimal lng;

        @Schema(description = "纬度")
        private BigDecimal lat;

        @Schema(description = "值")
        private Integer value;

        @Schema(description = "类型")
        private String type;
    }

    @Schema(description = "血液中心数据项")
    @Data
    public static class MainItem {

        @Schema(description = "主键")
        private Long id;

        @Schema(description = "运行机构名称")
        private String name;

        @Schema(description = "类型 1:一级、2:二级")
        private String level;

        @Schema(description = "运行状态 0:关闭，1:正常运行")
        private Integer state;

        @Schema(description = "经度")
        private BigDecimal lng;

        @Schema(description = "纬度")
        private BigDecimal lat;
    }

}
