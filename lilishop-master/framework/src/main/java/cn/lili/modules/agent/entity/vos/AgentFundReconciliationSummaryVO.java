package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 代理商资金对账汇总视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentFundReconciliationSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "流水总笔数")
    private Long totalCount;

    @Schema(description = "流水净额")
    private Double totalAmount;

    @Schema(description = "充值金额")
    private Double rechargeAmount;

    @Schema(description = "提现金额")
    private Double withdrawalAmount;

    @Schema(description = "余额支付金额")
    private Double payAmount;

    @Schema(description = "余额退款金额")
    private Double refundAmount;

    @Schema(description = "佣金金额")
    private Double commissionAmount;
}
