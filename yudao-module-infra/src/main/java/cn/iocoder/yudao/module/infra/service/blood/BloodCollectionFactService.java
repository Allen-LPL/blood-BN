package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;

public interface BloodCollectionFactService {

    PageResult<BloodCollectionFactDO> getBloodCollectionFactPage(BloodCollectionFactPageReqVO pageReqVO);

    BloodCollectionFactAggRespVO aggregate(BloodCollectionFactAggReqVO reqVO);

}
