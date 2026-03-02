package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 北京各区采血量统计 Response VO")
@Data
public class DistrictCollectionRespVO {

    @Schema(description = "各区采血量列表")
    private List<Item> items;

    @Schema(description = "区域采血量数据项")
    @Data
    public static class Item {

        @Schema(description = "区名称", example = "朝阳区")
        private String district;

        @Schema(description = "采血量（基础单位U）")
        private BigDecimal totalUnit;

        @Schema(description = "采血人次")
        private Long count;

    }

}
