package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供货端资产概览视图
 *
 * @author dawn
 * @since 2026/6/22
 */
@Data
public class StoreAssetOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "累计收益")
    private Double totalIncome;

    @Schema(description = "已结算金额")
    private Double settledAmount;

    @Schema(description = "可提现金额")
    private Double withdrawableAmount;

    @Schema(description = "冻结金额")
    private Double frozenAmount;
}
