package cn.iocoder.yudao.module.infra.dal.mysql.blood;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionMainSiteDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BloodCollectionMainSiteMapper extends BaseMapperX<BloodCollectionMainSiteDO> {

}
