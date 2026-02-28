package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.MetricOpEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.SupplyGroupByFieldEnum;
import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.SupplyMetricFieldEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 供血事实聚合 Request VO")
@Data
public class BloodSupplyFactAggReqVO {

    @Schema(description = "过滤条件")
    @Valid
    private BloodSupplyFactQueryReqVO filter;

    @Schema(description = "分组维度列表（支持编码、枚举名或别名）")
    @Size(max = 2, message = "groupBy 最多支持 2 个")
    @InEnum(value = SupplyGroupByFieldEnum.class, message = "groupBy 必须是 {value}")
    @JsonDeserialize(contentUsing = SupplyGroupByFieldCodeDeserializer.class)
    private List<Integer> groupBy;

    @Schema(description = "指标列表")
    @Size(min = 1, max = 5, message = "metrics 数量必须在 1 到 5 之间")
    @Valid
    private List<MetricSpec> metrics;

    @Schema(description = "排序配置")
    @Valid
    private OrderBySpec orderBy;

    @Schema(description = "返回条数限制，默认 100，最大 1000", example = "100")
    @Min(value = 1, message = "limit 必须大于 0")
    private Integer limit;

    @Schema(description = "指标定义")
    @Data
    public static class MetricSpec {

        @Schema(description = "聚合操作编码：1-COUNT,2-COUNT_DISTINCT,3-SUM,4-AVG", requiredMode = Schema.RequiredMode.REQUIRED)
        @InEnum(value = MetricOpEnum.class, message = "op 必须是 {value}")
        private Integer op;

        @Schema(description = "聚合字段编码：1-STAR,2-donation_code,3-product_code,4-base_unit_value", requiredMode = Schema.RequiredMode.REQUIRED)
        @InEnum(value = SupplyMetricFieldEnum.class, message = "field 必须是 {value}")
        private Integer field;

        @Schema(description = "指标别名（字母开头，只允许字母数字下划线）", example = "sumBaseUnit")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "alias 格式不正确")
        private String alias;

    }

    @Schema(description = "排序定义")
    @Data
    public static class OrderBySpec {

        @Schema(description = "排序字段（必须是 groupBy alias 或 metric alias）", example = "sumBaseUnit")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "orderBy.key 格式不正确")
        private String key;

        @Schema(description = "排序方向，ASC 或 DESC", example = "DESC")
        @Pattern(regexp = "^(ASC|DESC)$", message = "direction 仅支持 ASC 或 DESC")
        private String direction;

    }

}
