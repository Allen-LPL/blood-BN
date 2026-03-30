package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "医院被供血聚合统计查询 Request VO")
@Data
public class HospitalSupplyAggReqVO {

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "行政区列表（空或null表示全北京）")
    private List<String> districts;

    @Schema(description = "被供血类型（红细胞类、血小板类、血浆类；空或null表示全部）")
    private List<String> bloodProductTypes;

    @Schema(description = "统计周期：DAY-按日, MONTH-按月, YEAR-按年，默认 MONTH", example = "MONTH")
    @Pattern(regexp = "^(DAY|MONTH|YEAR)$", message = "统计周期仅支持 DAY、MONTH、YEAR")
    private String period;

}
