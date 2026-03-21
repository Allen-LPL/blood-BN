package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionSiteCoordinateRespVO;

public interface BloodCollectionSiteService {

    /**
     * 查询正在运行的采血点坐标数据
     *
     * @param operatingOrg 运行机构名称（精确匹配，可选）
     * @param district     所属区（精确匹配，可选）
     * @param type         类型（采血点类型，精确匹配，可选）
     * @return 采血点坐标列表 + 血液中心基础坐标列表
     */
    BloodCollectionSiteCoordinateRespVO getActiveCollectionSiteCoordinates(String operatingOrg, String district,
            String type);

}
