package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Schema(description = "管理后台 - 采供血趋势统计查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class BloodStatisticsTrendReqVO extends BloodStatisticsReqVO {

    @Schema(description = "统计周期：DAY-按日, WEEK-按周, MONTH-按月, YEAR-按年", requiredMode = Schema.RequiredMode.REQUIRED, example = "MONTH")
    @NotBlank(message = "统计周期不能为空")
    @Pattern(regexp = "^(DAY|WEEK|MONTH|YEAR)$", message = "统计周期仅支持 DAY、WEEK、MONTH、YEAR")
    private String period;

    @Schema(description = "趋势项（可多选）：自愿献血、团体无偿、全血采血量、血小板单采量", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"自愿献血\",\"全血采血量\"]")
    @NotEmpty(message = "趋势项至少选择一个")
    private List<String> trendTypes;

}
