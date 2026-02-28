package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 供血事实分页查询 Request VO")
@Data
public class BloodSupplyFactPageReqVO extends PageParam {

    @Schema(description = "献血码")
    private String donationCode;

    @Schema(description = "产品编码")
    private String productCode;

    @Schema(description = "导入批次 ID")
    private String loadBatchId;

    @Schema(description = "ABO 血型")
    private String abo;

    @Schema(description = "Rh(D) 血型")
    private String rhd;

    @Schema(description = "血液品种名称")
    private String bloodProductName;

    @Schema(description = "发放类型")
    private String issueType;

    @Schema(description = "发放机构")
    private String issuingOrg;

    @Schema(description = "接收机构")
    private String receivingOrg;

    @Schema(description = "接收机构所在行政区")
    private String receivingOrgAdminRegion;

    @Schema(description = "发放时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] issueTime;

    @Schema(description = "血液有效期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] bloodExpiryTime;

    @Schema(description = "入库时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] ingestedAt;

    @Schema(description = "快速检索关键词（仅匹配献血码/产品编码）")
    private String keyword;

}
