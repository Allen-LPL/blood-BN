package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 供血事实 Response VO")
@Data
public class BloodCollectionFactRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "采血部门")
    private String collectionDepartment;

    @Schema(description = "采血地点")
    private String collectionSite;

    @Schema(description = "组织方式")
    private String organizationMode;

    @Schema(description = "献血类型")
    private String donationType;

    @Schema(description = "档案ID")
    private String archiveId;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "所属单位组织单位")
    private String orgUnitName;

    @Schema(description = "单位属性")
    private String unitProperty;

    @Schema(description = "所属系统")
    private String systemName;

    @Schema(description = "单位级别")
    private String unitLevel;

    @Schema(description = "隶属单位")
    private String parentUnit;

    @Schema(description = "单位所在行政区")
    private String unitAdminRegion;

    @Schema(description = "建档日期")
    private LocalDateTime archiveCreatedDate;

    @Schema(description = "登记时间")
    private LocalDateTime registrationTime;

    @Schema(description = "初筛时间")
    private LocalDateTime precheckTime;

    @Schema(description = "采血时间")
    private LocalDateTime bloodCollectionTime;

    @Schema(description = "足量")
    private String fullVolumeFlag;

    @Schema(description = "不足量原因")
    private String insufficientReason;

    @Schema(description = "献血码")
    private String donationCode;

    @Schema(description = "档案血型")
    private String archiveBloodType;

    @Schema(description = "初筛血型")
    private String precheckBloodType;

    @Schema(description = "采血量")
    private String bloodVolume;

    @Schema(description = "基础单位(U)")
    private BigDecimal baseUnitValue;

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
