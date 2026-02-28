package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 供血事实聚合 Response VO")
@Data
public class BloodCollectionFactAggRespVO {

    @Schema(description = "分组字段别名")
    private List<String> groupBy;

    @Schema(description = "指标元信息")
    private List<MetricMeta> metrics;

    @Schema(description = "结果行")
    private List<Row> rows;

    @Schema(description = "指标元信息")
    @Data
    public static class MetricMeta {

        @Schema(description = "指标别名")
        private String alias;

        @Schema(description = "操作编码")
        private Integer op;

        @Schema(description = "字段编码")
        private Integer field;

    }

    @Schema(description = "聚合结果行")
    @Data
    public static class Row {

        @Schema(description = "分组 key 值")
        private Map<String, Object> keys;

        @Schema(description = "指标值")
        private Map<String, Object> values;

    }

}
