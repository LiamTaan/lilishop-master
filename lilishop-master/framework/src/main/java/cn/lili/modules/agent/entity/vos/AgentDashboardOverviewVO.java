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

    @Schema(description = "销售额")
    private Double salesAmount;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "绑定店铺数")
    private Long bindStoreCount;

    @Schema(description = "佣金金额")
    private Double commissionAmount;
}
