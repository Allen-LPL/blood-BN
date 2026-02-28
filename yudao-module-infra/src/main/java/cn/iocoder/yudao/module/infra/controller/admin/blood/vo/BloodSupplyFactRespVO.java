package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 供血事实 Response VO")
@Data
public class BloodSupplyFactRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "献血码")
    private String donationCode;

    @Schema(description = "产品编码")
    private String productCode;

    @Schema(description = "血液品种名称")
    private String bloodProductName;

    @Schema(description = "ABO 血型")
    private String abo;

    @Schema(description = "Rh(D) 血型")
    private String rhd;

    @Schema(description = "血液数量")
    private String bloodAmount;

    @Schema(description = "基础单位(U)")
    private BigDecimal baseUnitValue;

    @Schema(description = "血液有效期")
    private LocalDateTime bloodExpiryTime;

    @Schema(description = "发放时间")
    private LocalDateTime issueTime;

    @Schema(description = "发放类型")
    private String issueType;

    @Schema(description = "退回原因")
    private String returnReason;

    @Schema(description = "发放机构")
    private String issuingOrg;

    @Schema(description = "接收机构")
    private String receivingOrg;

    @Schema(description = "接收机构所在行政区")
    private String receivingOrgAdminRegion;

    @Schema(description = "来源文件名")
    private String sourceFile;

    @Schema(description = "来源Sheet名称")
    private String sheetName;

    @Schema(description = "来源行号")
    private Integer sourceRowNum;

    @Schema(description = "导入批次ID")
    private String loadBatchId;

    @Schema(description = "入库时间")
    private LocalDateTime ingestedAt;

}
