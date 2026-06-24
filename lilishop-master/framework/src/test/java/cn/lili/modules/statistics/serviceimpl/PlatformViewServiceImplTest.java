package cn.lili.modules.statistics.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.PlatformViewVO;
import cn.lili.modules.statistics.service.MemberStatisticsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformViewServiceImplTest {

    @InjectMocks
    private PlatformViewServiceImpl platformViewService;

    @Mock
    private MemberStatisticsService memberStatisticsService;

    @Mock
    private Cache cache;

    @Test
    void listShouldDefaultToTodayWhenQueryParamsAreMissing() {
        StatisticsQueryParam queryParam = new StatisticsQueryParam();
        when(cache.getString(any())).thenReturn(null);
        when(cache.counter(any())).thenReturn(0L);

        List<PlatformViewVO> result = platformViewService.list(queryParam);

        Assertions.assertEquals(1, result.size());
        Assertions.assertNotNull(result.get(0).getDate());
        Assertions.assertEquals(0L, result.get(0).getPvNum());
        Assertions.assertEquals(0L, result.get(0).getUvNum());
    }

    @Test
    void listShouldDefaultToTodayWhenMonthIsProvidedWithoutYear() {
        StatisticsQueryParam queryParam = new StatisticsQueryParam();
        queryParam.setMonth(6);
        when(cache.getString(any())).thenReturn(null);
        when(cache.counter(any())).thenReturn(0L);

        List<PlatformViewVO> result = platformViewService.list(queryParam);

        Assertions.assertEquals(1, result.size());
        Assertions.assertNotNull(result.get(0).getDate());
    }

    @Test
    void listShouldRejectInvalidMonthRange() {
        StatisticsQueryParam queryParam = new StatisticsQueryParam();
        queryParam.setYear(2026);
        queryParam.setMonth(13);

        Assertions.assertThrows(ServiceException.class, () -> platformViewService.list(queryParam));
    }
}
