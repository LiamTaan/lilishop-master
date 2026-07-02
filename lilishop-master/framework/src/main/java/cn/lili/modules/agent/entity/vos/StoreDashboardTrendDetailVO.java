package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供货端数据概览趋势明细视图
 *
 * @author dawn
 * @since 2026/7/2
 */
@Data
@Schema(description = "供货端数据概览趋势明细视图")
public class StoreDashboardTrendDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间点标签")
    private String label;

    @Schema(description = "销售成本")
    private Double salesCost;

    @Schema(description = "销售利润")
    private Double salesProfit;
}
