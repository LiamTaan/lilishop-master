package cn.lili.modules.agent.serviceimpl;

import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentDashboardOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.order.aftersale.mapper.AfterSaleMapper;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.mapper.OrderMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentDashboardServiceImplTest {

    @InjectMocks
    private AgentDashboardServiceImpl agentDashboardService;

    @Mock
    private AgentReconciliationService agentReconciliationService;

    @Mock
    private AgentStoreBindService agentStoreBindService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private AfterSaleMapper afterSaleMapper;

    @Test
    void overviewShouldReturnZeroMetricsWhenAgentHasNoBoundStore() {
        AgentFundReconciliationSummaryVO fundSummary = new AgentFundReconciliationSummaryVO();
        fundSummary.setCommissionAmount(12.5D);

        when(agentStoreBindService.listApprovedStoreIdsByAgentMemberId("agent-1")).thenReturn(Collections.emptyList());
        when(agentReconciliationService.fundSummary(eq("agent-1"), any(AgentFundReconciliationSearchParams.class)))
                .thenReturn(fundSummary);

        AgentDashboardOverviewVO overview = agentDashboardService.overview("agent-1");

        Assertions.assertEquals(12.5D, overview.getCommissionAmount());
        Assertions.assertEquals(0D, overview.getSalesAmount());
        Assertions.assertEquals(0L, overview.getOrderCount());
        Assertions.assertEquals(0L, overview.getTotalOrderCount());
        Assertions.assertEquals(0D, overview.getMonthIncome());
    }

    @Test
    void overviewShouldUseMonthlyOrdersForOrderCountAndAllOrdersForTotalCount() {
        AgentFundReconciliationSummaryVO fundSummary = new AgentFundReconciliationSummaryVO();
        fundSummary.setCommissionAmount(88D);

        List<String> storeIds = List.of("store-1");
        List<Order> allOrders = List.of(buildOrder(100D, 2), buildOrder(50D, 1), buildOrder(25D, 3));
        List<Order> monthOrders = List.of(buildOrder(100D, 2), buildOrder(50D, 1));
        List<Order> todayOrders = List.of(buildOrder(100D, 2));
        List<Order> sevenDayOrders = List.of(buildOrder(100D, 2), buildOrder(50D, 1));

        when(agentStoreBindService.listApprovedStoreIdsByAgentMemberId("agent-1")).thenReturn(storeIds);
        when(agentReconciliationService.fundSummary(eq("agent-1"), any(AgentFundReconciliationSearchParams.class)))
                .thenReturn(fundSummary);
        when(orderMapper.selectList(any()))
                .thenReturn(allOrders)
                .thenReturn(monthOrders)
                .thenReturn(todayOrders)
                .thenReturn(sevenDayOrders);
        when(afterSaleMapper.selectCount(any())).thenReturn(1L);

        AgentDashboardOverviewVO overview = agentDashboardService.overview("agent-1");

        Assertions.assertEquals(150D, overview.getSalesAmount());
        Assertions.assertEquals(2L, overview.getOrderCount());
        Assertions.assertEquals(3L, overview.getTotalOrderCount());
        Assertions.assertEquals(150D, overview.getMonthIncome());
        Assertions.assertEquals(2, overview.getTodaySales());
        Assertions.assertEquals(3, overview.getSevenDaySales());
        Assertions.assertEquals(58.33D, overview.getCustomerUnitPrice(), 0.0001D);
        Assertions.assertEquals(33.33D, overview.getReturnRate(), 0.0001D);
    }

    private Order buildOrder(double flowPrice, int goodsNum) {
        Order order = new Order();
        order.setFlowPrice(flowPrice);
        order.setGoodsNum(goodsNum);
        return order;
    }
}
