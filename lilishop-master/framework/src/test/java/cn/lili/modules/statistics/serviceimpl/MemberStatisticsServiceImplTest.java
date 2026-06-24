package cn.lili.modules.statistics.serviceimpl;

import cn.lili.modules.statistics.entity.dos.MemberStatisticsData;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemberStatisticsServiceImplTest {

    @Spy
    @InjectMocks
    private MemberStatisticsServiceImpl memberStatisticsService;

    @Test
    void statisticsShouldDefaultToTodayWhenQueryParamsAreMissing() {
        StatisticsQueryParam queryParam = new StatisticsQueryParam();
        doReturn(5L).when(memberStatisticsService).memberCount(any(Date.class));
        doReturn(2L).when(memberStatisticsService).activeQuantity(any(Date.class));
        doReturn(1L).when(memberStatisticsService).newlyAdded(any(Date.class), any(Date.class));

        List<MemberStatisticsData> result = memberStatisticsService.statistics(queryParam);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(5L, result.get(0).getMemberCount());
        Assertions.assertEquals(2L, result.get(0).getActiveQuantity());
        Assertions.assertEquals(1L, result.get(0).getNewlyAdded());
        Assertions.assertNotNull(result.get(0).getCreateDate());
    }
}
