package cn.lili.modules.agent.serviceimpl;

import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentDashboardOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.service.AgentDashboardService;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public AgentDashboardOverviewVO overview(String agentMemberId) {
        AgentProcurementReconciliationSummaryVO procurementSummary =
                agentReconciliationService.procurementSummary(agentMemberId, new AgentProcurementReconciliationSearchParams());
        AgentFundReconciliationSummaryVO fundSummary =
                agentReconciliationService.fundSummary(agentMemberId, new AgentFundReconciliationSearchParams());

        AgentDashboardOverviewVO vo = new AgentDashboardOverviewVO();
        vo.setSalesAmount(procurementSummary.getTotalAmount() == null ? 0D : procurementSummary.getTotalAmount());
        vo.setOrderCount(procurementSummary.getTotalCount() == null ? 0L : procurementSummary.getTotalCount());
        vo.setBindStoreCount((long) agentStoreBindService.listApprovedBindsByAgentMemberId(agentMemberId).size());
        vo.setCommissionAmount(fundSummary.getCommissionAmount() == null ? 0D : fundSummary.getCommissionAmount());
        return vo;
    }
}
