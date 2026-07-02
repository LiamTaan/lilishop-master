package cn.lili.modules.agent.entity.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供货端数据概览趋势查询参数
 *
 * @author dawn
 * @since 2026/7/2
 */
@Data
public class StoreDashboardTrendQueryParams {

    @Schema(description = "趋势范围", allowableValues = "WEEK,MONTH,YEAR")
    private String range;
}
