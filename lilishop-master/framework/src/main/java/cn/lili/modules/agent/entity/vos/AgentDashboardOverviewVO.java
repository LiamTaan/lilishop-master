package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 代理端经营概览视图
 *
 * @author dawn
 * @since 2026/6/22
 */
@Data
public class AgentDashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "本月销售额")
    private Double salesAmount;

    @Schema(description = "本月订单数")
    private Long orderCount;

    @Schema(description = "绑定店铺数")
    private Long bindStoreCount;

    @Schema(description = "佣金金额")
    private Double commissionAmount;

    @Schema(description = "本月成交额")
    private Double monthIncome;

    @Schema(description = "今日销量")
    private Integer todaySales;

    @Schema(description = "近7日销量")
    private Integer sevenDaySales;

    @Schema(description = "累计订单数")
    private Long totalOrderCount;

    @Schema(description = "客单价")
    private Double customerUnitPrice;

    @Schema(description = "退货率")
    private Double returnRate;
}
