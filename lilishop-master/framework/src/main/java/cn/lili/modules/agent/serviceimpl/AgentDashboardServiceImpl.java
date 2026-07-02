package cn.lili.modules.agent.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentDashboardOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.service.AgentDashboardService;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.aftersale.mapper.AfterSaleMapper;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.PayStatusEnum;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.order.trade.entity.enums.AfterSaleStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 代理端经营概览服务实现
 *
 * @author dawn
 * @since 2026/6/22
 */
@Service
public class AgentDashboardServiceImpl implements AgentDashboardService {

    @Autowired
    private AgentReconciliationService agentReconciliationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AfterSaleMapper afterSaleMapper;

    @Override
    public AgentDashboardOverviewVO overview(String agentMemberId) {
        List<String> storeIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(agentMemberId);
        AgentFundReconciliationSummaryVO fundSummary = agentReconciliationService.fundSummary(
                agentMemberId,
                new AgentFundReconciliationSearchParams()
        );

        AgentDashboardOverviewVO vo = new AgentDashboardOverviewVO();
        vo.setCommissionAmount(fundSummary.getCommissionAmount() == null ? 0D : fundSummary.getCommissionAmount());
        vo.setBindStoreCount((long) storeIds.size());

        if (storeIds.isEmpty()) {
            fillEmptyOrderMetrics(vo);
            return vo;
        }

        Date now = new Date();
        Date todayStart = DateUtil.beginOfDay(now);
        Date todayEnd = DateUtil.endOfDay(now);
        Date monthStart = DateUtil.beginOfMonth(now);
        Date sevenDayStart = DateUtil.beginOfDay(DateUtil.offsetDay(now, -6));

        List<Order> allOrders = orderMapper.selectList(this.validOrderWrapper(storeIds));
        List<Order> monthOrders = orderMapper.selectList(this.validOrderWrapper(storeIds)
                .ge(Order::getCreateTime, monthStart)
                .le(Order::getCreateTime, todayEnd));
        List<Order> todayOrders = orderMapper.selectList(this.validOrderWrapper(storeIds)
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd));
        List<Order> sevenDayOrders = orderMapper.selectList(this.validOrderWrapper(storeIds)
                .ge(Order::getCreateTime, sevenDayStart)
                .le(Order::getCreateTime, todayEnd));

        double totalSalesAmount = sumAmount(allOrders);
        long monthOrderCount = monthOrders.size();
        long totalOrderCount = allOrders.size();
        double monthIncome = sumAmount(monthOrders);
        long completedAfterSaleCount = afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
                .in(AfterSale::getStoreId, storeIds)
                .eq(AfterSale::getServiceStatus, AfterSaleStatusEnum.COMPLETE.name()));

        vo.setSalesAmount(monthIncome);
        vo.setOrderCount(monthOrderCount);
        vo.setMonthIncome(monthIncome);
        vo.setTodaySales(sumGoodsNum(todayOrders));
        vo.setSevenDaySales(sumGoodsNum(sevenDayOrders));
        vo.setTotalOrderCount(totalOrderCount);
        vo.setCustomerUnitPrice(totalOrderCount == 0 ? 0D : CurrencyUtil.div(totalSalesAmount, totalOrderCount));
        vo.setReturnRate(totalOrderCount == 0 ? 0D : CurrencyUtil.div(CurrencyUtil.mul(completedAfterSaleCount, 100), totalOrderCount));
        return vo;
    }

    private LambdaQueryWrapper<Order> validOrderWrapper(List<String> storeIds) {
        return Wrappers.<Order>lambdaQuery()
                .in(Order::getStoreId, storeIds)
                .eq(Order::getPayStatus, PayStatusEnum.PAID.name())
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.name());
    }

    private double sumAmount(List<Order> orders) {
        return orders == null ? 0D : orders.stream()
                .map(order -> order.getFlowPrice() == null ? 0D : order.getFlowPrice())
                .reduce(0D, CurrencyUtil::add);
    }

    private int sumGoodsNum(List<Order> orders) {
        return orders == null ? 0 : orders.stream()
                .map(order -> order.getGoodsNum() == null ? 0 : order.getGoodsNum())
                .reduce(0, Integer::sum);
    }

    private void fillEmptyOrderMetrics(AgentDashboardOverviewVO vo) {
        vo.setSalesAmount(0D);
        vo.setOrderCount(0L);
        vo.setMonthIncome(0D);
        vo.setTodaySales(0);
        vo.setSevenDaySales(0);
        vo.setTotalOrderCount(0L);
        vo.setCustomerUnitPrice(0D);
        vo.setReturnRate(0D);
    }
}
