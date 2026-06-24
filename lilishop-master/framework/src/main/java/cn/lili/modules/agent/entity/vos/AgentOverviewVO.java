package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 代理商经营概览视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "代理商数量")
    private Long agentCount;

    @Schema(description = "已绑定店铺数量")
    private Long bindStoreCount;

    @Schema(description = "代理商资金流水总额")
    private Double totalFundAmount;

    @Schema(description = "代理商佣金总额")
    private Double commissionAmount;
}
