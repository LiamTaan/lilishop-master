package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供货端数据概览视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@Schema(description = "供货端数据概览视图")
public class StoreDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "本周销售利润")
    private Double newProfit;

    @Schema(description = "销售利润较上周变化比例")
    private Double newProfitWow;

    @Schema(description = "本周订单总量")
    private Long orderTotal;

    @Schema(description = "订单总量较上周变化比例")
    private Double orderTotalWow;

    @Schema(description = "本周供需对接成功率")
    private Double supplyMatchSuccessRate;

    @Schema(description = "供需对接成功率较上周变化比例")
    private Double supplyMatchSuccessRateWow;

    @Schema(description = "本周下单转化率")
    private Double orderConversionRate;

    @Schema(description = "下单转化率较上周变化比例")
    private Double orderConversionRateWow;

    @Schema(description = "本周客单价")
    private Double customerUnitPrice;

    @Schema(description = "客单价较上周变化比例")
    private Double customerUnitPriceWow;

    @Schema(description = "本周退货率")
    private Double returnRate;

    @Schema(description = "退货率较上周变化比例")
    private Double returnRateWow;
}
