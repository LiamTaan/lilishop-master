package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.vos.AgentDashboardOverviewVO;

/**
 * 代理端经营概览服务
 *
 * @author dawn
 * @since 2026/6/22
 */
public interface AgentDashboardService {

    /**
     * 查询当前代理商经营概览
     *
     * @param agentMemberId 代理商会员ID
     * @return 经营概览
     */
    AgentDashboardOverviewVO overview(String agentMemberId);
}
