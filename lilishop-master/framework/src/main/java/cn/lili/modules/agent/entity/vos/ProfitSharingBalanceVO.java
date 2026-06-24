package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台分账余额视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class ProfitSharingBalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应结总额")
    private Double totalBillAmount;

    @Schema(description = "已核对总额")
    private Double checkedAmount;

    @Schema(description = "已付款总额")
    private Double paidAmount;

    @Schema(description = "退款总额")
    private Double refundAmount;

    @Schema(description = "待分账总额")
    private Double pendingShareAmount;
}
