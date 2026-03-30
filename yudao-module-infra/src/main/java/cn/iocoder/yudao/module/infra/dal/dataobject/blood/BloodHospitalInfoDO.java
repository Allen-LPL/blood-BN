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

@TableName("blood_hospital_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class BloodHospitalInfoDO {
    @TableId
    private Long id;
    /** 医院编码 */
    private String bianMa;
    /** 行政区域 */
    private String xingZhengQuYu;
    /** 医院全称 */
    private String yiYuanQuanCheng;
    /** 机构属性(公立/民营/其他) */
    private String jiGouShuXing;
    /** 机构类别 */
    private String jiGouLeiBie;
    /** 经度 */
    private BigDecimal jingDu;
    /** 纬度 */
    private BigDecimal weiDu;
    /** 医院类型 */
    private String yiYuanLeiXing;
    /** 医院专业类型 */
    private String yiYuanZhuanYeLeiXing;
    /** 导入批次ID */
    private String loadBatchId;
    /** 导入时间 */
    private LocalDateTime ingestedAt;
}
