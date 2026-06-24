package cn.lili.modules.agent.entity.vos;

import cn.lili.modules.order.aftersale.entity.vo.AfterSaleManageSummaryVO;
import cn.lili.modules.order.order.entity.vo.OrderManageSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationExceptionSummaryVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 批发平台治理总览
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class WholesaleGovernanceSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单治理汇总")
    private OrderManageSummaryVO orderSummary;

    @Schema(description = "售后治理汇总")
    private AfterSaleManageSummaryVO afterSaleSummary;

    @Schema(description = "分账治理汇总")
    private ProfitSharingSummaryVO profitSharingSummary;

    @Schema(description = "核销异常治理汇总")
    private VerificationExceptionSummaryVO verificationExceptionSummary;
}
