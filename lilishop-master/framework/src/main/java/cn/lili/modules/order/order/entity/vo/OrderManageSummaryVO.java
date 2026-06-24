package cn.lili.modules.order.order.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单治理汇总视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class OrderManageSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单总数")
    private Long totalCount;

    @Schema(description = "订单总金额")
    private Double totalFlowPrice;

    @Schema(description = "待付款订单数")
    private Integer waitPayNum;

    @Schema(description = "已付款订单数")
    private Integer waitDeliveryNum;

    @Schema(description = "待发货订单数")
    private Integer waitShipNum;

    @Schema(description = "待收货订单数")
    private Integer deliveredNum;

    @Schema(description = "待核销订单数")
    private Integer waitCheckNum;

    @Schema(description = "待自提订单数")
    private Integer waitSelfPickNum;

    @Schema(description = "已完成订单数")
    private Integer finishNum;

    @Schema(description = "已关闭订单数")
    private Integer closeNum;
}
