package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentReconciliationOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationVO;
import cn.lili.modules.agent.service.AgentReconciliationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端代理商对账治理接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "管理端,代理商对账治理接口")
@RequestMapping("/manager/agent/reconciliation")
public class AgentReconciliationManagerController {

    @Autowired
    private AgentReconciliationService agentReconciliationService;

    @Operation(summary = "查询平台代理商对账总览")
    @GetMapping("/overview")
    public ResultMessage<AgentReconciliationOverviewVO> overview() {
        return ResultUtil.data(agentReconciliationService.overview());
    }

    @Operation(summary = "分页查询代理商采购对账")
    @GetMapping("/{agentMemberId}/procurement")
    public ResultMessage<IPage<AgentProcurementReconciliationVO>> procurementPage(@PathVariable String agentMemberId,
                                                                                  AgentProcurementReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.procurementPage(agentMemberId, searchParams));
    }

    @Operation(summary = "查询代理商采购对账汇总")
    @GetMapping("/{agentMemberId}/procurement/summary")
    public ResultMessage<AgentProcurementReconciliationSummaryVO> procurementSummary(@PathVariable String agentMemberId,
                                                                                     AgentProcurementReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.procurementSummary(agentMemberId, searchParams));
    }

    @Operation(summary = "查询代理商采购对账详情")
    @Parameter(name = "id", description = "采购单ID", required = true)
    @GetMapping("/{agentMemberId}/procurement/{id}")
    public ResultMessage<AgentProcurementReconciliationVO> procurementDetail(@PathVariable String agentMemberId,
                                                                             @PathVariable String id) {
        return ResultUtil.data(agentReconciliationService.procurementDetail(agentMemberId, id));
    }

    @Operation(summary = "查询代理商采购对账导出列表")
    @GetMapping("/{agentMemberId}/procurement/list")
    public ResultMessage<List<AgentProcurementReconciliationVO>> procurementList(@PathVariable String agentMemberId,
                                                                                 AgentProcurementReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.procurementList(agentMemberId, searchParams));
    }

    @Operation(summary = "分页查询代理商资金对账")
    @GetMapping("/{agentMemberId}/fund")
    public ResultMessage<IPage<AgentFundReconciliationVO>> fundPage(@PathVariable String agentMemberId,
                                                                    AgentFundReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.fundPage(agentMemberId, searchParams));
    }

    @Operation(summary = "查询代理商资金对账汇总")
    @GetMapping("/{agentMemberId}/fund/summary")
    public ResultMessage<AgentFundReconciliationSummaryVO> fundSummary(@PathVariable String agentMemberId,
                                                                       AgentFundReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.fundSummary(agentMemberId, searchParams));
    }

    @Operation(summary = "查询代理商资金对账详情")
    @Parameter(name = "id", description = "资金流水ID", required = true)
    @GetMapping("/{agentMemberId}/fund/{id}")
    public ResultMessage<AgentFundReconciliationVO> fundDetail(@PathVariable String agentMemberId,
                                                               @PathVariable String id) {
        return ResultUtil.data(agentReconciliationService.fundDetail(agentMemberId, id));
    }

    @Operation(summary = "查询代理商资金对账导出列表")
    @GetMapping("/{agentMemberId}/fund/list")
    public ResultMessage<List<AgentFundReconciliationVO>> fundList(@PathVariable String agentMemberId,
                                                                   AgentFundReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.fundList(agentMemberId, searchParams));
    }
}
