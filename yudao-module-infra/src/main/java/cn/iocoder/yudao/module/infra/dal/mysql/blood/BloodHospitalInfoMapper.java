package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodHospitalInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Mapper
public interface BloodHospitalInfoMapper extends BaseMapperX<BloodHospitalInfoDO> {

    /**
     * 查询机构类别非"部队医院"的医院信息列表
     */
    @Select("SELECT * FROM blood_hospital_info WHERE ji_gou_lei_bie != '部队医院'")
    List<BloodHospitalInfoDO> selectNonMilitaryHospitals();

    /**
     * 查询非部队医院，支持按行政区过滤（districts 为空时不过滤区域）
     */
    default List<BloodHospitalInfoDO> selectNonMilitaryHospitalsByDistricts(List<String> districts) {
        LambdaQueryWrapperX<BloodHospitalInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.ne(BloodHospitalInfoDO::getJiGouLeiBie, "部队医院");
        wrapper.inIfPresent(BloodHospitalInfoDO::getXingZhengQuYu, districts);
        return selectList(wrapper);
    }

    /**
     * 多条件查询医院，支持行政区、机构属性、医院类型过滤（不含部队医院）
     */
    default List<BloodHospitalInfoDO> selectByFilters(List<String> districts, String jiGouShuXing,
            String yiYuanLeiXing) {
        LambdaQueryWrapperX<BloodHospitalInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.ne(BloodHospitalInfoDO::getJiGouLeiBie, "部队医院");
        wrapper.inIfPresent(BloodHospitalInfoDO::getXingZhengQuYu, districts);
        wrapper.inIfPresent(BloodHospitalInfoDO::getJiGouShuXing, StringUtils.split(yiYuanLeiXing, ","));
        wrapper.eqIfPresent(BloodHospitalInfoDO::getYiYuanLeiXing, yiYuanLeiXing);
        return selectList(wrapper);
    }

    /**
     * 供血分布查询：不排除部队医院，支持机构属性/机构类别/医院类型/医院专业类型四维过滤
     */
    default List<BloodHospitalInfoDO> selectAllByDistributionFilters(String jiGouShuXing, String jiGouLeiBie,
            String yiYuanLeiXing,
            String yiYuanZhuanYeLeiXing) {
        LambdaQueryWrapperX<BloodHospitalInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(BloodHospitalInfoDO::getJiGouShuXing, jiGouShuXing);
        wrapper.eqIfPresent(BloodHospitalInfoDO::getJiGouLeiBie, jiGouLeiBie);
        wrapper.eqIfPresent(BloodHospitalInfoDO::getYiYuanLeiXing, yiYuanLeiXing);
        wrapper.eqIfPresent(BloodHospitalInfoDO::getYiYuanZhuanYeLeiXing, yiYuanZhuanYeLeiXing);
        return selectList(wrapper);
    }

}
