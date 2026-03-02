package cn.iocoder.yudao.module.infra.dal.dataobject.blood;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 运行机构名称
     */
    private String collectionOperatingOrg;
    /**
     * 类型 1:一级、2:二级
     */
    private Integer type;
    /**
     * 运行状态 0:关闭，1:正常运行
     */
    private Integer state;
    /**
     * 经度
     */
    private String coordinateLng;
    /**
     * 纬度
     */
    private String coordinateLat;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}
