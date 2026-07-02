package cn.lili.modules.agent.serviceimpl;

import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;
import cn.lili.modules.agent.service.StoreDashboardAnalyticsService;
import cn.lili.modules.store.entity.dos.Bill;
import cn.lili.modules.store.entity.enums.BillStatusEnum;
import cn.lili.modules.store.service.BillService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreDashboardServiceImplTest {

    @InjectMocks
    private StoreDashboardServiceImpl storeDashboardService;

    @Mock
    private StoreDashboardAnalyticsService storeDashboardAnalyticsService;

    @Mock
    private BillService billService;

    @Test
    void dashboardShouldDelegateToAnalyticsService() {
        StoreWorkbenchOverviewVO overview = new StoreWorkbenchOverviewVO();
        overview.setOrderTotal(8L);
        when(storeDashboardAnalyticsService.overview("store-1")).thenReturn(overview);

        StoreWorkbenchOverviewVO result = storeDashboardService.dashboard("store-1");

        Assertions.assertSame(overview, result);
        verify(storeDashboardAnalyticsService).overview("store-1");
    }

    @Test
    void trendShouldDelegateToAnalyticsService() {
        StoreDashboardTrendQueryParams params = new StoreDashboardTrendQueryParams();
        params.setRange("WEEK");
        StoreDashboardTrendVO trendVO = new StoreDashboardTrendVO();
        trendVO.setLabels(List.of("07-01"));
        when(storeDashboardAnalyticsService.trend("store-1", params)).thenReturn(trendVO);

        StoreDashboardTrendVO result = storeDashboardService.trend("store-1", params);

        Assertions.assertSame(trendVO, result);
        verify(storeDashboardAnalyticsService).trend("store-1", params);
    }

    @Test
    void trendDetailShouldDelegateToAnalyticsService() {
        StoreDashboardTrendQueryParams params = new StoreDashboardTrendQueryParams();
        params.setRange("MONTH");
        StoreDashboardTrendDetailVO detailVO = new StoreDashboardTrendDetailVO();
        detailVO.setLabel("07-02");
        when(storeDashboardAnalyticsService.trendDetail(eq("store-1"), any())).thenReturn(List.of(detailVO));

        List<StoreDashboardTrendDetailVO> result = storeDashboardService.trendDetail("store-1", params);

        Assertions.assertEquals(1, result.size());
        verify(storeDashboardAnalyticsService).trendDetail("store-1", params);
    }

    @Test
    void assetsShouldAggregateBillsByStatus() {
        Bill complete = new Bill();
        complete.setBillStatus(BillStatusEnum.COMPLETE.name());
        complete.setBillPrice(100D);
        Bill check = new Bill();
        check.setBillStatus(BillStatusEnum.CHECK.name());
        check.setBillPrice(50D);
        Bill out = new Bill();
        out.setBillStatus(BillStatusEnum.OUT.name());
        out.setBillPrice(20D);
        when(billService.list(org.mockito.ArgumentMatchers.<QueryWrapper<Bill>>any())).thenReturn(List.of(complete, check, out));

        StoreAssetOverviewVO result = storeDashboardService.assets("store-1");

        Assertions.assertEquals(170D, result.getTotalIncome());
        Assertions.assertEquals(100D, result.getSettledAmount());
        Assertions.assertEquals(50D, result.getWithdrawableAmount());
        Assertions.assertEquals(20D, result.getFrozenAmount());
    }
}
