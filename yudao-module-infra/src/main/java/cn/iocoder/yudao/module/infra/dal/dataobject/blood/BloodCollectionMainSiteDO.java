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

/**
 * 北京血液中心信息 DO
 *
 * 对应表：blood_collection_main_site
 */
@TableName("blood_collection_main_site")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodCollectionMainSiteDO {

    @TableId
    private Long id;
    /**
     * 行政区
     */
    private String district;
    /**
     * 采血机构名称
     */
    private String collectionOperatingOrg;
    /**
     * 是否独立法人(1:是,0:否)
     */
    private Integer isIndependentLegalEntity;
    /**
     * 社会信用代码
     */
    private String socialCreditCode;
    /**
     * 建设时间
     */
    private String constructionTime;
    /**
     * 具体地址
     */
    private String specificAddress;
    /**
     * 所属街道
     */
    private String street;
    /**
     * 依托单位
     */
    private String supportingUnit;
    /**
     * 经度
     */
    private BigDecimal coordinateLng;
    /**
     * 纬度
     */
    private BigDecimal coordinateLat;
    /**
     * 采血点数量
     */
    private Integer siteCount;
    /**
     * 运行采血点数量
     */
    private Integer operatingSiteCount;
    /**
     * 负责人
     */
    private String personInCharge;
    /**
     * 联系电话
     */
    private String contactPhone;
    /**
     * 类型 1:一级、2:二级
     */
    private Integer type;
    /**
     * 运行状态 0:关闭，1:正常运行
     */
    private Integer state;
    /**
     * 导入批次ID
     */
    private String loadBatchId;
    /**
     * 导入时间
     */
    private LocalDateTime ingestedAt;

}
