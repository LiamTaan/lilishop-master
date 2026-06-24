package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台分账治理汇总
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class ProfitSharingSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "账单总数")
    private Long totalCount;

    @Schema(description = "待分账账单数")
    private Long pendingCount;

    @Schema(description = "已核对账单数")
    private Long checkedCount;

    @Schema(description = "已完成账单数")
    private Long completedCount;

    @Schema(description = "账单应结总额")
    private Double totalBillAmount;

    @Schema(description = "账单佣金总额")
    private Double totalCommissionAmount;

    @Schema(description = "账单退款总额")
    private Double totalRefundAmount;
}
