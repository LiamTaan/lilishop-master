package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 供货端数据概览趋势图视图
 *
 * @author dawn
 * @since 2026/7/2
 */
@Data
@Schema(description = "供货端数据概览趋势图视图")
public class StoreDashboardTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "横轴标签")
    private List<String> labels;

    @Schema(description = "销售成本序列")
    private List<Double> salesCostSeries;

    @Schema(description = "销售利润序列")
    private List<Double> salesProfitSeries;
}
