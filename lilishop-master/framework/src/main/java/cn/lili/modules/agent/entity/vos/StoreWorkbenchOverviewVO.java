package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供货商工作台首页聚合视图
 *
 * @author dawn
 * @since 2026/7/2
 */
@Data
@Schema(description = "供货商工作台首页聚合视图")
public class StoreWorkbenchOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日营业额")
    private Double todayTurnover;

    @Schema(description = "今日营业额较昨日变化比例")
    private Double todayTurnoverCompareRate;

    @Schema(description = "今日营业额对比文案")
    private String todayTurnoverCompareLabel;

    @Schema(description = "订单总量")
    private Long orderTotal;

    @Schema(description = "订单总量较昨日变化比例")
    private Double orderTotalCompareRate;

    @Schema(description = "订单总量对比文案")
    private String orderTotalCompareLabel;

    @Schema(description = "在售商品数")
    private Long onSaleGoodsCount;

    @Schema(description = "在售分类数")
    private Long onSaleCategoryCount;

    @Schema(description = "库存预警数")
    private Long stockWarningCount;

    @Schema(description = "库存预警提示文案")
    private String stockWarningHint;

    @Schema(description = "最新平台公告")
    private StoreWorkbenchMessageVO noticeMessage;

    @Schema(description = "最新店铺治理消息")
    private StoreWorkbenchMessageVO governanceMessage;

    @Schema(description = "待发货数量")
    private Long pendingShipmentCount;

    @Schema(description = "待售后数量")
    private Long pendingAfterSaleCount;

    @Schema(description = "物流异常数量")
    private Long logisticsExceptionCount;

    @Schema(description = "售后总数")
    private Long afterSaleCount;
}
