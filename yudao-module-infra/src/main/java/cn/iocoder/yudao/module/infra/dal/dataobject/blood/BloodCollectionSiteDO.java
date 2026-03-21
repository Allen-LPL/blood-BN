package cn.iocoder.yudao.module.infra.dal.dataobject.blood;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 献血点信息 DO
 *
 * 对应表：blood_collection_site
 */
@TableName("blood_collection_site")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodCollectionSiteDO {

    @TableId
    private Long id;

    /**
     * 采血机构名称
     */
    private String operatingOrg;

    /**
     * 行政区域
     */
    private String district;

    /**
     * 献血点名称
     */
    private String collectionSiteName;

    /**
     * 献血点(备案)名称
     */
    private String siteNameFiling;

    /**
     * 献血点(系统)名称
     */
    private String siteNameSystem;

    /**
     * 经度
     */
    private BigDecimal coordinateLng;

    /**
     * 纬度
     */
    private BigDecimal coordinateLat;

    /**
     * 献血点设置区域
     */
    private String siteSetupAreaType;

    /**
     * 献血点类型(方舱/屋/车)
     */
    private String siteType;

    /**
     * 首次运行日期
     */
    private LocalDate firstOperationDate;

    /**
     * 运行状态(1:运行,0:停运)
     */
    private Integer state;

    /**
     * 日常开放时间
     */
    private String dailyOpenTime;

    /**
     * 具体地址
     */
    private String specificAddress;

    /**
     * 导入批次ID
     */
    private String loadBatchId;

    /**
     * 导入时间
     */
    private LocalDateTime ingestedAt;

}
