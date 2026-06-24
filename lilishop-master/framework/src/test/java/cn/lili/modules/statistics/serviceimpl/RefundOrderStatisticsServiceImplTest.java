package cn.lili.modules.statistics.serviceimpl;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.RefundOrderStatisticsDataVO;
import cn.lili.modules.statistics.mapper.RefundOrderStatisticsMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundOrderStatisticsServiceImplTest {

    @Spy
    @InjectMocks
    private RefundOrderStatisticsServiceImpl refundOrderStatisticsService;

    @Mock
    private RefundOrderStatisticsMapper refundOrderStatisticsMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refundOrderStatisticsService, "baseMapper", refundOrderStatisticsMapper);
    }

    @Test
    void getRefundOrderStatisticsDataShouldDefaultWhenQueryParamsAreMissing() {
        IPage<RefundOrderStatisticsDataVO> page = new Page<>(1, 10);
        when(refundOrderStatisticsMapper.getRefundStatisticsData(any(IPage.class), any(QueryWrapper.class))).thenReturn(page);

        IPage<RefundOrderStatisticsDataVO> result =
                refundOrderStatisticsService.getRefundOrderStatisticsData(new PageVO(), new StatisticsQueryParam());

        Assertions.assertSame(page, result);
        verify(refundOrderStatisticsMapper).getRefundStatisticsData(any(IPage.class), any(QueryWrapper.class));
    }

    @Test
    void getRefundOrderStatisticsPriceShouldReturnZeroWhenPriceIsMissing() {
        doReturn(null).when(refundOrderStatisticsService).getMap(any(QueryWrapper.class));

        Double result = refundOrderStatisticsService.getRefundOrderStatisticsPrice(new StatisticsQueryParam());

        Assertions.assertEquals(0D, result);
    }

    @Test
    void getRefundOrderStatisticsPriceShouldReturnActualPriceWhenPresent() {
        doReturn(Collections.singletonMap("price", 88.5D)).when(refundOrderStatisticsService).getMap(any(QueryWrapper.class));

        Double result = refundOrderStatisticsService.getRefundOrderStatisticsPrice(new StatisticsQueryParam());

        Assertions.assertEquals(88.5D, result);
    }
}
