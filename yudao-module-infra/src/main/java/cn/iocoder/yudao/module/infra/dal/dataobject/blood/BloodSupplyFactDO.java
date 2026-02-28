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

@TableName("blood_supply_fact")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodSupplyFactDO {

    @TableId
    private Long id;
    private String donationCode;
    private String productCode;
    private String bloodProductName;
    private String abo;
    private String rhd;
    private String bloodAmount;
    private BigDecimal baseUnitValue;
    private LocalDateTime bloodExpiryTime;
    private LocalDateTime issueTime;
    private String issueType;
    private String returnReason;
    private String issuingOrg;
    private String receivingOrg;
    private String receivingOrgAdminRegion;
    private String sourceFile;
    private String sheetName;
    private Integer sourceRowNum;
    private String loadBatchId;
    private LocalDateTime ingestedAt;

}
