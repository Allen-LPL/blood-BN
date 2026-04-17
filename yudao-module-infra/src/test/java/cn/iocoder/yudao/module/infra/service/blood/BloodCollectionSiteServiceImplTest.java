package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionSiteCoordinateRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionMainSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionSiteDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodHospitalInfoDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionMainSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodHospitalInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodCollectionSiteServiceImplTest {

    @Mock
    private BloodCollectionSiteMapper bloodCollectionSiteMapper;

    @Mock
    private BloodCollectionMainSiteMapper bloodCollectionMainSiteMapper;

    @Mock
    private BloodHospitalInfoMapper bloodHospitalInfoMapper;

    @InjectMocks
    private BloodCollectionSiteServiceImpl bloodCollectionSiteService;

    @Test
    void getActiveCollectionSiteCoordinates_shouldIncludeHospitalItemsAndUseNonMilitaryHospitals() {
        BloodCollectionSiteDO site = new BloodCollectionSiteDO();
        site.setCollectionSiteName("朝阳献血屋");
        site.setCoordinateLng(new BigDecimal("116.480"));
        site.setCoordinateLat(new BigDecimal("39.990"));
        site.setState(1);

        BloodCollectionMainSiteDO mainSite = new BloodCollectionMainSiteDO();
        mainSite.setId(1L);
        mainSite.setCollectionOperatingOrg("北京市红十字血液中心");
        mainSite.setState(1);
        mainSite.setCoordinateLng(new BigDecimal("116.410"));
        mainSite.setCoordinateLat(new BigDecimal("39.910"));

        BloodHospitalInfoDO hospital = BloodHospitalInfoDO.builder()
                .yiYuanQuanCheng("北京协和医院")
                .jingDu(new BigDecimal("116.420"))
                .weiDu(new BigDecimal("39.930"))
                .yiYuanLeiXing("综合医院")
                .xingZhengQuYu("东城区")
                .build();

        when(bloodCollectionSiteMapper.selectList(any())).thenReturn(Collections.singletonList(site));
        when(bloodCollectionMainSiteMapper.selectList(any())).thenReturn(Collections.singletonList(mainSite));
        when(bloodHospitalInfoMapper.selectNonMilitaryHospitalsByDistricts(Collections.singletonList("东城区")))
                .thenReturn(Collections.singletonList(hospital));

        BloodCollectionSiteCoordinateRespVO resp = bloodCollectionSiteService.getActiveCollectionSiteCoordinates(
                null, "东城区", null);

        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
        assertEquals(1, resp.getMainItems().size());
        assertEquals(1, resp.getHospitalItems().size());
        assertEquals("北京协和医院", resp.getHospitalItems().get(0).getYiYuanQuanCheng());
        assertEquals(new BigDecimal("116.420"), resp.getHospitalItems().get(0).getJingDu());
        assertEquals(new BigDecimal("39.930"), resp.getHospitalItems().get(0).getWeiDu());
        assertEquals("综合医院", resp.getHospitalItems().get(0).getYiYuanLeiXing());
        assertEquals("东城区", resp.getHospitalItems().get(0).getXingZhengQuYu());
        verify(bloodHospitalInfoMapper).selectNonMilitaryHospitalsByDistricts(Collections.singletonList("东城区"));
    }

}
