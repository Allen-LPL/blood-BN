package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "医院供血分布查询 Request VO。" +
        "分组规则：① 默认（均不填）→ 按机构属性(公立/民营/其他)分组；" +
        "② 填 jiGouShuXing → 按机构类别(部属/市属/区属/民营/部队医院等)细分；" +
        "③ 填 yiYuanLeiXing → 按医院专业类型(20类)细分；" +
        "④ jiGouShuXing 和 jiGouLeiBie 均填 → 两者作为过滤条件，按机构类别展示；" +
        "⑤ yiYuanLeiXing 和 yiYuanZhuanYeLeiXing 均填 → 两者作为过滤，按专业类型展示。")
@Data
public class HospitalBloodDistributionReqVO {

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "机构属性 L1（公立/民营/其他）；填此项时按机构类别 L2 分组")
    private String jiGouShuXing;

    @Schema(description = "机构类别 L2（部属/市属/区属/其他/民营/部队医院等）；与 jiGouShuXing 搭配作为过滤条件")
    private String jiGouLeiBie;

    @Schema(description = "医院类型 L1（综合医院/专科医院/中医医院/妇幼保健院/中西医结合医院/其他）；填此项时按专业类型 L2 分组")
    private String yiYuanLeiXing;

    @Schema(description = "医院专业类型 L2（20类细分）；与 yiYuanLeiXing 搭配作为过滤条件")
    private String yiYuanZhuanYeLeiXing;

}
