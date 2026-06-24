package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.PlatformFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.PlatformProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentReconciliationOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationVO;
import cn.lili.modules.agent.entity.vos.PlatformFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.PlatformProcurementReconciliationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 代理商对账业务接口
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface AgentReconciliationService {

    /**
     * 查询代理商采购对账分页
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<AgentProcurementReconciliationVO> procurementPage(String agentMemberId, AgentProcurementReconciliationSearchParams params);

    /**
     * 查询代理商采购对账汇总
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 汇总结果
     */
    AgentProcurementReconciliationSummaryVO procurementSummary(String agentMemberId, AgentProcurementReconciliationSearchParams params);

    /**
     * 查询代理商采购对账详情
     *
     * @param agentMemberId 代理商会员ID
     * @param id 采购单ID
     * @return 详情
     */
    AgentProcurementReconciliationVO procurementDetail(String agentMemberId, String id);

    /**
     * 查询代理商资金对账分页
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<AgentFundReconciliationVO> fundPage(String agentMemberId, AgentFundReconciliationSearchParams params);

    /**
     * 查询代理商资金对账汇总
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 汇总结果
     */
    AgentFundReconciliationSummaryVO fundSummary(String agentMemberId, AgentFundReconciliationSearchParams params);

    /**
     * 查询代理商资金对账详情
     *
     * @param agentMemberId 代理商会员ID
     * @param id 流水ID
     * @return 详情
     */
    AgentFundReconciliationVO fundDetail(String agentMemberId, String id);

    /**
     * 查询平台代理商对账总览
     *
     * @return 总览结果
     */
    AgentReconciliationOverviewVO overview();

    /**
     * 查询代理商采购对账导出列表
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 列表
     */
    List<AgentProcurementReconciliationVO> procurementList(String agentMemberId, AgentProcurementReconciliationSearchParams params);

    /**
     * 查询代理商资金对账导出列表
     *
     * @param agentMemberId 代理商会员ID
     * @param params 查询参数
     * @return 列表
     */
    List<AgentFundReconciliationVO> fundList(String agentMemberId, AgentFundReconciliationSearchParams params);

    /**
     * 查询平台采购对账分页
     *
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<PlatformProcurementReconciliationVO> platformProcurementPage(PlatformProcurementReconciliationSearchParams params);

    /**
     * 查询平台资金对账分页
     *
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<PlatformFundReconciliationVO> platformFundPage(PlatformFundReconciliationSearchParams params);
}
