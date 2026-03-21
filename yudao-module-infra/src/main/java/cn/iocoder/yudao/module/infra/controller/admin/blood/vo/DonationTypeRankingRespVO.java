package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 献血类型排行 Response VO")
@Data
public class DonationTypeRankingRespVO {

    @Schema(description = "献血类型排行列表")
    private List<Item> items;

    @Schema(description = "献血类型排行数据项")
    @Data
    public static class Item {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "类型名称")
        private String typeName;

        @Schema(description = "采血量（基础单位U）")
        private BigDecimal totalUnit;

        @Schema(description = "采血人次")
        private Long count;
    }
}
