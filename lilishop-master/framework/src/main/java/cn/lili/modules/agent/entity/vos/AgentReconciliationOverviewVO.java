package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台代理商对账总览
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentReconciliationOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "代理商数量")
    private Long agentCount;

    @Schema(description = "采购对账汇总")
    private AgentProcurementReconciliationSummaryVO procurementSummary;

    @Schema(description = "资金对账汇总")
    private AgentFundReconciliationSummaryVO fundSummary;
}
