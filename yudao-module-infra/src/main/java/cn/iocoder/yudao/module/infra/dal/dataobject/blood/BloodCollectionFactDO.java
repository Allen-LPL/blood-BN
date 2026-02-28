package cn.iocoder.yudao.module.infra.dal.dataobject.blood;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("blood_collection_fact")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodCollectionFactDO {

    @TableId
    private Long id;
    private String collectionDepartment;
    private String collectionSite;
    private String organizationMode;
    private String donationType;
    private String archiveId;
    private String gender;
    private Integer age;
    private String orgUnitName;
    private String unitProperty;
    private String systemName;
    private String unitLevel;
    private String parentUnit;
    private String unitAdminRegion;
    private LocalDateTime archiveCreatedDate;
    private LocalDateTime registrationTime;
    private LocalDateTime precheckTime;
    private LocalDateTime bloodCollectionTime;
    private String fullVolumeFlag;
    private String insufficientReason;
    private String donationCode;
    private String precheckResult;
    private String precheckFailItems;
    private String archiveBloodType;
    private String precheckBloodType;
    private String bloodVolume;
    private BigDecimal baseUnitValue;
    private String recheckResult;
    private String recheckFailItems;
    private String sourceFile;
    private String sheetName;
    private Integer sourceRowNum;
    private String loadBatchId;
    private LocalDateTime ingestedAt;

}
