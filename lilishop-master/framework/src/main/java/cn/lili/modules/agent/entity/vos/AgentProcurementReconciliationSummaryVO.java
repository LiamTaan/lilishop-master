package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 代理商采购对账汇总视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentProcurementReconciliationSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "采购单总数")
    private Long totalCount;

    @Schema(description = "采购总金额")
    private Double totalAmount;

    @Schema(description = "采购总数量")
    private Integer totalQuantity;

    @Schema(description = "待入库单数")
    private Long pendingInboundCount;

    @Schema(description = "已完成单数")
    private Long completedCount;

    @Schema(description = "已关闭单数")
    private Long closedCount;
}
