package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionSiteCoordinateRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionMainSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodHospitalInfoDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionMainSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodHospitalInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Validated
public class BloodCollectionSiteServiceImpl implements BloodCollectionSiteService {

    @Resource
    private BloodCollectionSiteMapper bloodCollectionSiteMapper;

    @Resource
    private BloodCollectionMainSiteMapper bloodCollectionMainSiteMapper;

    @Resource
    private BloodHospitalInfoMapper bloodHospitalInfoMapper;

    @Override
    public BloodCollectionSiteCoordinateRespVO getActiveCollectionSiteCoordinates(String operatingOrg, String district,
            String type) {
        // 1) 构建查询条件：state=1 + 可选的 operatingOrg/district 精确匹配
        LambdaQueryWrapperX<BloodCollectionSiteDO> queryWrapper = new LambdaQueryWrapperX<BloodCollectionSiteDO>()
                .eq(BloodCollectionSiteDO::getState, 1)
                .eqIfPresent(BloodCollectionSiteDO::getOperatingOrg, operatingOrg)
                .eqIfPresent(BloodCollectionSiteDO::getDistrict, district);
        // .eqIfPresent(BloodCollectionSiteDO::getSiteType, type);

        List<BloodCollectionSiteDO> sites = bloodCollectionSiteMapper.selectList(queryWrapper);

        // 2) 转换为响应VO
        List<BloodCollectionSiteCoordinateRespVO.Item> items = new ArrayList<>();
        for (BloodCollectionSiteDO site : sites) {
            BloodCollectionSiteCoordinateRespVO.Item item = new BloodCollectionSiteCoordinateRespVO.Item();
            item.setSiteNameSystem(site.getSiteNameSystem());
            item.setCollectionSiteName(site.getCollectionSiteName());
            item.setSiteNameFiling(site.getSiteNameFiling());
            item.setDailyOpenTime(site.getDailyOpenTime());
            item.setSpecificAddress(site.getSpecificAddress());
            item.setDistrict(site.getDistrict());
            item.setOperatingOrg(site.getOperatingOrg());
            item.setLng(site.getCoordinateLng());
            item.setLat(site.getCoordinateLat());
            item.setValue(site.getState());
            item.setType(site.getSiteType());
            items.add(item);
        }

        BloodCollectionSiteCoordinateRespVO resp = new BloodCollectionSiteCoordinateRespVO();
        resp.setItems(items);
        resp.setMainItems(getActiveMainSiteBaseItems(operatingOrg, district));
        resp.setHospitalItems(getHospitalItems(district));
        return resp;
    }

    private List<BloodCollectionSiteCoordinateRespVO.MainItem> getActiveMainSiteBaseItems(String operatingOrg,
            String district) {
        LambdaQueryWrapperX<BloodCollectionMainSiteDO> queryWrapper = new LambdaQueryWrapperX<BloodCollectionMainSiteDO>()
                .eq(BloodCollectionMainSiteDO::getState, 1)
                .eqIfPresent(BloodCollectionMainSiteDO::getCollectionOperatingOrg, operatingOrg)
                .eqIfPresent(BloodCollectionMainSiteDO::getDistrict, district);

        List<BloodCollectionMainSiteDO> mainSites = bloodCollectionMainSiteMapper.selectList(queryWrapper);
        List<BloodCollectionSiteCoordinateRespVO.MainItem> mainItems = new ArrayList<>();
        for (BloodCollectionMainSiteDO mainSite : mainSites) {
            BloodCollectionSiteCoordinateRespVO.MainItem item = new BloodCollectionSiteCoordinateRespVO.MainItem();
            item.setId(mainSite.getId());
            item.setName(mainSite.getCollectionOperatingOrg());
            item.setLevel(mainSite.getType() != null && mainSite.getType() == 1 ? "一级" : "二级");
            item.setState(mainSite.getState());
            item.setLng(mainSite.getCoordinateLng());
            item.setLat(mainSite.getCoordinateLat());
            mainItems.add(item);
        }
        return mainItems;
    }

    private List<BloodCollectionSiteCoordinateRespVO.HospitalItem> getHospitalItems(String district) {
        List<BloodHospitalInfoDO> hospitals = bloodHospitalInfoMapper.selectNonMilitaryHospitalsByDistricts(
                StringUtils.hasText(district) ? Collections.singletonList(district) : null);
        List<BloodCollectionSiteCoordinateRespVO.HospitalItem> hospitalItems = new ArrayList<>();
        for (BloodHospitalInfoDO hospital : hospitals) {
            BloodCollectionSiteCoordinateRespVO.HospitalItem item = new BloodCollectionSiteCoordinateRespVO.HospitalItem();
            item.setYiYuanQuanCheng(hospital.getYiYuanQuanCheng());
            item.setJingDu(hospital.getJingDu());
            item.setWeiDu(hospital.getWeiDu());
            item.setYiYuanLeiXing(hospital.getYiYuanLeiXing());
            item.setXingZhengQuYu(hospital.getXingZhengQuYu());
            item.setJiGouShuXing(hospital.getJiGouShuXing());
            hospitalItems.add(item);
        }
        return hospitalItems;
    }

}
