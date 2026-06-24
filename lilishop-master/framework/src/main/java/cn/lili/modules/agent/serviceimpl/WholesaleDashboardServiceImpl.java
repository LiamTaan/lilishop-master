package cn.lili.modules.agent.serviceimpl;

import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingBalanceVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingSummaryVO;
import cn.lili.modules.agent.entity.vos.WholesaleBusinessTrendVO;
import cn.lili.modules.agent.entity.vos.WholesaleDashboardVO;
import cn.lili.modules.agent.entity.vos.WholesaleGovernanceSummaryVO;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.agent.service.ProfitSharingService;
import cn.lili.modules.agent.service.WholesaleDashboardService;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleManageSummaryVO;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleSearchParams;
import cn.lili.modules.order.aftersale.service.AfterSaleService;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.order.order.entity.dto.OrderSearchParams;
import cn.lili.modules.order.order.entity.vo.OrderManageSummaryVO;
import cn.lili.modules.order.order.service.OrderService;
import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.store.service.StoreService;
import cn.lili.modules.statistics.entity.dto.GoodsStatisticsQueryParam;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.enums.StatisticsQuery;
import cn.lili.modules.statistics.entity.vo.CategoryStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.IndexStatisticsVO;
import cn.lili.modules.statistics.entity.vo.StoreStatisticsDataVO;
import cn.lili.modules.statistics.service.IndexStatisticsService;
import cn.lili.modules.statistics.service.StoreFlowStatisticsService;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationExceptionSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordSummaryVO;
import cn.lili.modules.verification.service.VerificationRecordService;
import cn.lili.modules.wallet.entity.dos.WalletLog;
import cn.lili.modules.wallet.mapper.WalletLogMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批发商城平台工作台服务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class WholesaleDashboardServiceImpl implements WholesaleDashboardService {

    @Autowired
    private IndexStatisticsService indexStatisticsService;

    @Autowired
    private ProfitSharingService profitSharingService;

    @Autowired
    private AgentReconciliationService agentReconciliationService;

    @Autowired
    private WalletLogMapper walletLogMapper;

    @Autowired
    private StoreFlowStatisticsService storeFlowStatisticsService;

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private VerificationRecordService verificationRecordService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AfterSaleService afterSaleService;

    @Override
    public WholesaleDashboardVO dashboard() {
        WholesaleDashboardVO vo = new WholesaleDashboardVO();
        IndexStatisticsVO indexStatistics = indexStatisticsService.indexStatistics();
        StatisticsQueryParam statisticsQueryParam = new StatisticsQueryParam();
        GoodsStatisticsQueryParam goodsStatisticsQueryParam = new GoodsStatisticsQueryParam();
        goodsStatisticsQueryParam.setType(StatisticsQuery.PRICE.name());
        List<StoreStatisticsDataVO> storeRankList = indexStatisticsService.storeStatistics(statisticsQueryParam);
        List<GoodsStatisticsDataVO> goodsRankList = indexStatisticsService.goodsStatistics(goodsStatisticsQueryParam);
        List<CategoryStatisticsDataVO> categoryRankList = storeFlowStatisticsService.getCategoryStatisticsData(goodsStatisticsQueryParam);
        ProfitSharingBalanceVO profitSharingBalance = profitSharingService.balance();
        AgentFundReconciliationSummaryVO agentFundSummary = this.buildAgentFundSummary();
        AgentProcurementReconciliationSummaryVO procurementSummary = this.buildProcurementSummary();
        StoreAuditSummaryVO storeAuditSummary = storeService.managementSummary();
        VerificationRecordSummaryVO verificationSummary = verificationRecordService.summary(new VerificationRecordSearchParams());
        WholesaleGovernanceSummaryVO governanceSummary = this.buildGovernanceSummary();
        AgentOverviewVO agentOverview = this.buildAgentOverview(agentFundSummary);
        WholesaleBusinessTrendVO businessTrend = this.buildBusinessTrend(indexStatistics, profitSharingBalance, procurementSummary, storeAuditSummary, verificationSummary);
        vo.setIndexStatistics(indexStatistics);
        vo.setStoreRankList(storeRankList);
        vo.setGoodsRankList(goodsRankList);
        vo.setCategoryRankList(categoryRankList);
        vo.setProfitSharingBalance(profitSharingBalance);
        vo.setAgentFundSummary(agentFundSummary);
        vo.setProcurementSummary(procurementSummary);
        vo.setStoreAuditSummary(storeAuditSummary);
        vo.setBusinessTrend(businessTrend);
        vo.setVerificationSummary(verificationSummary);
        vo.setGovernanceSummary(governanceSummary);
        vo.setAgentOverview(agentOverview);
        return vo;
    }

    private AgentFundReconciliationSummaryVO buildAgentFundSummary() {
        AgentFundReconciliationSummaryVO summary = new AgentFundReconciliationSummaryVO();
        List<String> agentMemberIds = agentRoleRelationService.list(Wrappers.<AgentRoleRelation>lambdaQuery()
                        .select(AgentRoleRelation::getMemberId))
                .stream()
                .map(AgentRoleRelation::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        if (agentMemberIds.isEmpty()) {
            summary.setTotalCount(0L);
            summary.setTotalAmount(0D);
            summary.setRechargeAmount(0D);
            summary.setWithdrawalAmount(0D);
            summary.setPayAmount(0D);
            summary.setRefundAmount(0D);
            summary.setCommissionAmount(0D);
            return summary;
        }
        List<WalletLog> walletLogs = walletLogMapper.selectList(Wrappers.<WalletLog>lambdaQuery()
                .in(WalletLog::getMemberId, agentMemberIds));
        if (walletLogs == null || walletLogs.isEmpty()) {
            summary.setTotalCount(0L);
            summary.setTotalAmount(0D);
            summary.setRechargeAmount(0D);
            summary.setWithdrawalAmount(0D);
            summary.setPayAmount(0D);
            summary.setRefundAmount(0D);
            summary.setCommissionAmount(0D);
            return summary;
        }
        double totalAmount = 0D;
        double rechargeAmount = 0D;
        double withdrawalAmount = 0D;
        double payAmount = 0D;
        double refundAmount = 0D;
        double commissionAmount = 0D;
        for (WalletLog walletLog : walletLogs) {
            double money = walletLog.getMoney() == null ? 0D : walletLog.getMoney();
            totalAmount = CurrencyUtil.add(totalAmount, money);
            String serviceType = walletLog.getServiceType();
            if ("WALLET_RECHARGE".equals(serviceType)) {
                rechargeAmount = CurrencyUtil.add(rechargeAmount, money);
            } else if ("WALLET_WITHDRAWAL".equals(serviceType)) {
                withdrawalAmount = CurrencyUtil.add(withdrawalAmount, money);
            } else if ("WALLET_PAY".equals(serviceType)) {
                payAmount = CurrencyUtil.add(payAmount, money);
            } else if ("WALLET_REFUND".equals(serviceType)) {
                refundAmount = CurrencyUtil.add(refundAmount, money);
            } else if ("WALLET_COMMISSION".equals(serviceType)) {
                commissionAmount = CurrencyUtil.add(commissionAmount, money);
            }
        }
        summary.setTotalCount((long) walletLogs.size());
        summary.setTotalAmount(totalAmount);
        summary.setRechargeAmount(rechargeAmount);
        summary.setWithdrawalAmount(withdrawalAmount);
        summary.setPayAmount(payAmount);
        summary.setRefundAmount(refundAmount);
        summary.setCommissionAmount(commissionAmount);
        return summary;
    }

    private AgentOverviewVO buildAgentOverview(AgentFundReconciliationSummaryVO agentFundSummary) {
        AgentOverviewVO vo = new AgentOverviewVO();
        long agentCount = agentRoleRelationService.count(Wrappers.lambdaQuery());
        long bindStoreCount = agentStoreBindService.count(Wrappers.lambdaQuery());
        vo.setAgentCount(agentCount);
        vo.setBindStoreCount(bindStoreCount);
        vo.setTotalFundAmount(agentFundSummary.getTotalAmount());
        vo.setCommissionAmount(agentFundSummary.getCommissionAmount());
        return vo;
    }

    private AgentProcurementReconciliationSummaryVO buildProcurementSummary() {
        AgentProcurementReconciliationSummaryVO summary = new AgentProcurementReconciliationSummaryVO();
        summary.setTotalCount(0L);
        summary.setTotalAmount(0D);
        summary.setTotalQuantity(0);
        summary.setPendingInboundCount(0L);
        summary.setCompletedCount(0L);
        summary.setClosedCount(0L);
        agentRoleRelationService.list(Wrappers.lambdaQuery()).forEach(agent -> {
            AgentProcurementReconciliationSummaryVO item = agentReconciliationService.procurementSummary(agent.getMemberId(), new AgentProcurementReconciliationSearchParams());
            summary.setTotalCount(summary.getTotalCount() + item.getTotalCount());
            summary.setTotalAmount(summary.getTotalAmount() + item.getTotalAmount());
            summary.setTotalQuantity(summary.getTotalQuantity() + item.getTotalQuantity());
            summary.setPendingInboundCount(summary.getPendingInboundCount() + item.getPendingInboundCount());
            summary.setCompletedCount(summary.getCompletedCount() + item.getCompletedCount());
            summary.setClosedCount(summary.getClosedCount() + item.getClosedCount());
        });
        return summary;
    }

    private WholesaleBusinessTrendVO buildBusinessTrend(IndexStatisticsVO indexStatistics,
                                                        ProfitSharingBalanceVO profitSharingBalance,
                                                        AgentProcurementReconciliationSummaryVO procurementSummary,
                                                        StoreAuditSummaryVO storeAuditSummary,
                                                        VerificationRecordSummaryVO verificationSummary) {
        WholesaleBusinessTrendVO vo = new WholesaleBusinessTrendVO();
        vo.setTodayOrderPrice(indexStatistics.getTodayOrderPrice());
        vo.setYesterdayUv(indexStatistics.getYesterdayUV());
        vo.setTodayUv(indexStatistics.getTodayUV());
        vo.setLastSevenUv(indexStatistics.getLastSevenUV());
        vo.setPendingShareAmount(profitSharingBalance.getPendingShareAmount());
        vo.setPendingInboundCount(procurementSummary.getPendingInboundCount());
        vo.setSubmittedStoreCount(storeAuditSummary.getSubmittedCount());
        vo.setVerificationExceptionCount(verificationSummary.getExceptionCount());
        return vo;
    }

    private WholesaleGovernanceSummaryVO buildGovernanceSummary() {
        WholesaleGovernanceSummaryVO summaryVO = new WholesaleGovernanceSummaryVO();
        OrderManageSummaryVO orderSummary = orderService.getOrderManageSummaryVO(new OrderSearchParams());
        AfterSaleManageSummaryVO afterSaleSummary = afterSaleService.getAfterSaleManageSummaryVO(new AfterSaleSearchParams());
        ProfitSharingSummaryVO profitSharingSummary = profitSharingService.summary(null);
        VerificationExceptionSummaryVO verificationExceptionSummary = verificationRecordService.exceptionSummary(new VerificationRecordSearchParams());
        summaryVO.setOrderSummary(orderSummary);
        summaryVO.setAfterSaleSummary(afterSaleSummary);
        summaryVO.setProfitSharingSummary(profitSharingSummary);
        summaryVO.setVerificationExceptionSummary(verificationExceptionSummary);
        return summaryVO;
    }
}
