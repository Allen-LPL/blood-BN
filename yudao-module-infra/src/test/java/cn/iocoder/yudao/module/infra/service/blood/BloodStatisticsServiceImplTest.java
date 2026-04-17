package cn.iocoder.yudao.module.infra.service.blood;

import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionFactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodCollectionMainSiteMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.blood.BloodSupplyFactMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodStatisticsServiceImplTest {

    @Mock
    private BloodCollectionFactMapper collectionMapper;

    @Mock
    private BloodCollectionMainSiteMapper mainSiteMapper;

    @Mock
    private BloodSupplyFactMapper supplyMapper;

    @InjectMocks
    private BloodStatisticsServiceImpl bloodStatisticsService;

    @BeforeEach
    void setUp() {
        when(collectionMapper.selectMaps(any(QueryWrapperX.class))).thenReturn(Collections.<Map<String, Object>>emptyList());
    }

    @Test
    void getWholeBloodTrend_whenVoluntarySelected_shouldIncludePersonalAndVoluntaryOrgModes() {
        bloodStatisticsService.getWholeBloodTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                "MONTH",
                "北京市红十字血液中心",
                Collections.singletonList("自愿献血"));

        QueryWrapperX<BloodCollectionFactDO> query = captureTrendQuery();
        assertTrue(query.getCustomSqlSegment().contains("collection_department"));
        assertTrue(query.getCustomSqlSegment().contains("organization_mode"));
        assertTrue(query.getParamNameValuePairs().values().contains("北京市红十字血液中心"));
        assertTrue(query.getParamNameValuePairs().values().contains("个人无偿"));
        assertTrue(query.getParamNameValuePairs().values().contains("自愿献血"));
        assertFalse(query.getParamNameValuePairs().values().contains("团体无偿"));
    }

    @Test
    void getWholeBloodTrend_whenGroupSelected_shouldIncludeGroupAndHospitalGroupOrgModes() {
        bloodStatisticsService.getWholeBloodTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                "MONTH",
                null,
                Collections.singletonList("团体无偿"));

        QueryWrapperX<BloodCollectionFactDO> query = captureTrendQuery();
        assertTrue(query.getCustomSqlSegment().contains("organization_mode"));
        assertTrue(query.getParamNameValuePairs().values().contains("团体无偿"));
        assertTrue(query.getParamNameValuePairs().values().contains("医院团体"));
        assertFalse(query.getParamNameValuePairs().values().contains("个人无偿"));
    }

    @Test
    void getWholeBloodTrend_whenVoluntaryAndGroupSelected_shouldNotFilterByOrganizationMode() {
        bloodStatisticsService.getWholeBloodTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                "MONTH",
                null,
                Arrays.asList("自愿献血", "团体无偿"));

        QueryWrapperX<BloodCollectionFactDO> query = captureTrendQuery();
        assertFalse(query.getCustomSqlSegment().contains("organization_mode"));
        assertFalse(query.getParamNameValuePairs().values().contains("个人无偿"));
        assertFalse(query.getParamNameValuePairs().values().contains("医院团体"));
    }

    @Test
    void getWholeBloodTrend_whenVoluntaryAndWholeBloodSelected_shouldCombineFiltersWithAnd() {
        bloodStatisticsService.getWholeBloodTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                "MONTH",
                null,
                Arrays.asList("自愿献血", "全血采血量"));

        QueryWrapperX<BloodCollectionFactDO> query = captureTrendQuery();
        assertTrue(query.getCustomSqlSegment().contains("organization_mode"));
        assertTrue(query.getCustomSqlSegment().contains("blood_volume IS NOT NULL AND CAST(blood_volume AS DECIMAL(10,2)) >= 50"));
        assertFalse(query.getCustomSqlSegment().contains("organization_mode = '自愿献血' OR blood_volume"));
        assertTrue(query.getParamNameValuePairs().values().contains("个人无偿"));
        assertTrue(query.getParamNameValuePairs().values().contains("自愿献血"));
    }

    @SuppressWarnings("unchecked")
    private QueryWrapperX<BloodCollectionFactDO> captureTrendQuery() {
        ArgumentCaptor<QueryWrapperX> captor = ArgumentCaptor.forClass(QueryWrapperX.class);
        verify(collectionMapper).selectMaps(captor.capture());
        return captor.getValue();
    }

}
