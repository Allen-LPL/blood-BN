package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;

public interface BloodSupplyFactService {

    PageResult<BloodSupplyFactDO> getBloodSupplyFactPage(BloodSupplyFactPageReqVO pageReqVO);

    BloodSupplyFactAggRespVO aggregate(BloodSupplyFactAggReqVO reqVO);

}
