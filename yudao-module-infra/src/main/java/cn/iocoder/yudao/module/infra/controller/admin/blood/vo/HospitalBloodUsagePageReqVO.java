package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "医疗机构用血量分页查询 Request VO")
@Data
public class HospitalBloodUsagePageReqVO extends PageParam {

    public HospitalBloodUsagePageReqVO() {
        setPageSize(20);
    }

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "行政区列表（空或null表示全部）")
    private List<String> districts;

    @Schema(description = "机构属性（公立/民营/其他；空或null表示全部）")
    private String jiGouShuXing;

    @Schema(description = "医院类型（空或null表示全部）")
    private String yiYuanLeiXing;

}
