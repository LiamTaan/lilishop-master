package cn.lili.modules.agent.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.message.entity.dos.StoreMessage;
import cn.lili.modules.message.mapper.StoreMessageMapper;
import cn.lili.modules.order.aftersale.mapper.AfterSaleMapper;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.mapper.ProcurementInboundMapper;
import cn.lili.modules.statistics.service.GoodsStatisticsService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreDashboardAnalyticsServiceImplTest {

    @InjectMocks
    private StoreDashboardAnalyticsServiceImpl analyticsService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private AfterSaleMapper afterSaleMapper;

    @Mock
    private ProcurementInboundMapper procurementInboundMapper;

    @Mock
    private GoodsStatisticsService goodsStatisticsService;

    @Mock
    private StoreMessageMapper storeMessageMapper;

    @Test
    void overviewShouldReturnWorkbenchMetrics() {
        Order todayPaid1 = new Order();
        todayPaid1.setFlowPrice(200D);
        Order todayPaid2 = new Order();
        todayPaid2.setFlowPrice(100D);
        Order yesterdayPaid = new Order();
        yesterdayPaid.setFlowPrice(150D);

        Goods goods1 = new Goods();
        goods1.setCategoryPath("100,200,300");
        Goods goods2 = new Goods();
        goods2.setCategoryPath("100,200,301");
        Goods goods3 = new Goods();
        goods3.setCategoryPath("100,200,300");

        StoreMessage noticeMessage = new StoreMessage();
        noticeMessage.setId("msg-1");
        noticeMessage.setTitle("公告");
        noticeMessage.setContent("有商品售罄，请尽快补货");
        noticeMessage.setBizType("PLATFORM");

        StoreMessage governanceMessage = new StoreMessage();
        governanceMessage.setId("msg-2");
        governanceMessage.setTitle("违规");
        governanceMessage.setContent("店铺被处罚，请及时处理");
        governanceMessage.setBizType("STORE_GOVERNANCE");

        when(orderMapper.selectList(any()))
                .thenReturn(List.of(todayPaid1, todayPaid2))
                .thenReturn(List.of(yesterdayPaid));
        when(orderMapper.selectCount(any()))
                .thenReturn(10L)
                .thenReturn(5L)
                .thenReturn(666L);
        when(goodsStatisticsService.goodsNum(any(), any(), any())).thenReturn(68L);
        when(goodsStatisticsService.list(org.mockito.ArgumentMatchers.<Wrapper<Goods>>any())).thenReturn(List.of(goods1, goods2, goods3));
        when(goodsStatisticsService.alertQuantityNum("store-1")).thenReturn(87L);
        when(storeMessageMapper.selectLatestByStoreIdAndBizType("store-1", "PLATFORM")).thenReturn(noticeMessage);
        when(storeMessageMapper.selectLatestByStoreIdAndBizType("store-1", "STORE_GOVERNANCE")).thenReturn(governanceMessage);
        when(afterSaleMapper.selectCount(any())).thenReturn(8L).thenReturn(50L);

        StoreWorkbenchOverviewVO result = analyticsService.overview("store-1");

        Assertions.assertEquals(300D, result.getTodayTurnover());
        Assertions.assertEquals(100D, result.getTodayTurnoverCompareRate());
        Assertions.assertEquals("较昨日", result.getTodayTurnoverCompareLabel());
        Assertions.assertEquals(10L, result.getOrderTotal());
        Assertions.assertEquals(100D, result.getOrderTotalCompareRate());
        Assertions.assertEquals(68L, result.getOnSaleGoodsCount());
        Assertions.assertEquals(2L, result.getOnSaleCategoryCount());
        Assertions.assertEquals(87L, result.getStockWarningCount());
        Assertions.assertEquals("需要补货", result.getStockWarningHint());
        Assertions.assertEquals("公告", result.getNoticeMessage().getTitle());
        Assertions.assertEquals("违规", result.getGovernanceMessage().getTitle());
        Assertions.assertEquals(666L, result.getPendingShipmentCount());
        Assertions.assertEquals(8L, result.getPendingAfterSaleCount());
        Assertions.assertEquals(0L, result.getLogisticsExceptionCount());
        Assertions.assertEquals(50L, result.getAfterSaleCount());
    }

    @Test
    void trendShouldBuildSeriesForRequestedRange() {
        Date now = new Date();
        Order paidOrder = new Order();
        paidOrder.setPaymentTime(now);
        paidOrder.setFlowPrice(180D);

        ProcurementInbound inbound = new ProcurementInbound();
        inbound.setInboundTime(now);
        inbound.setTotalCost(60D);

        when(orderMapper.selectList(any())).thenReturn(List.of(paidOrder));
        when(procurementInboundMapper.selectList(any())).thenReturn(List.of(inbound));

        StoreDashboardTrendQueryParams params = new StoreDashboardTrendQueryParams();
        params.setRange("WEEK");

        StoreDashboardTrendVO trend = analyticsService.trend("store-1", params);
        List<StoreDashboardTrendDetailVO> detail = analyticsService.trendDetail("store-1", params);

        Assertions.assertFalse(trend.getLabels().isEmpty());
        Assertions.assertEquals(trend.getLabels().size(), trend.getSalesCostSeries().size());
        Assertions.assertEquals(trend.getLabels().size(), trend.getSalesProfitSeries().size());
        Assertions.assertEquals(trend.getLabels().size(), detail.size());
        int lastIndex = trend.getLabels().size() - 1;
        String expectedLabel = DateUtil.format(DateUtil.beginOfDay(now), "MM-dd");
        Assertions.assertEquals(expectedLabel, detail.get(lastIndex).getLabel());
        Assertions.assertEquals(60D, detail.get(lastIndex).getSalesCost());
        Assertions.assertEquals(120D, detail.get(lastIndex).getSalesProfit());
    }
}
