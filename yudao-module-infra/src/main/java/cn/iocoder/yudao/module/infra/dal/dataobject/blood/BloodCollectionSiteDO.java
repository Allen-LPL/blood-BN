package cn.iocoder.yudao.module.infra.dal.dataobject.blood;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("blood_collection_site")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodCollectionSiteDO {

    @TableId
    private Long id;
    private String district;
    private String collectionSiteName;
    private String operatingOrg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer state;

}
