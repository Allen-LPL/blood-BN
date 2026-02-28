package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 供血事实分页查询 Request VO")
@Data
public class BloodCollectionFactPageReqVO extends PageParam {

    @Schema(description = "档案 ID")
    private String archiveId;

    @Schema(description = "献血码")
    private String donationCode;

    @Schema(description = "导入批次 ID")
    private String loadBatchId;

    @Schema(description = "采血部门")
    private String collectionDepartment;

    @Schema(description = "采血地点")
    private String collectionSite;

    @Schema(description = "组织方式")
    private String organizationMode;

    @Schema(description = "献血类型")
    private String donationType;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "足量标记")
    private String fullVolumeFlag;

    @Schema(description = "档案血型")
    private String archiveBloodType;

    @Schema(description = "初筛血型")
    private String precheckBloodType;

    @Schema(description = "单位所在行政区")
    private String unitAdminRegion;

    @Schema(description = "所属单位组织单位，模糊匹配")
    private String orgUnitName;

    @Schema(description = "所属系统，模糊匹配")
    private String systemName;

    @Schema(description = "隶属单位，模糊匹配")
    private String parentUnit;

    @Schema(description = "年龄最小值")
    private Integer ageMin;

    @Schema(description = "年龄最大值")
    private Integer ageMax;

    @Schema(description = "登记时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] registrationTime;

    @Schema(description = "初筛时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] precheckTime;

    @Schema(description = "采血时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] bloodCollectionTime;

    @Schema(description = "入库时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] ingestedAt;

    @Schema(description = "快速检索关键词（仅匹配献血码/档案ID）")
    private String keyword;

}
