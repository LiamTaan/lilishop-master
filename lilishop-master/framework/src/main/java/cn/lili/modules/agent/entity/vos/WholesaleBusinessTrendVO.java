package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 批发平台经营趋势与健康度视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class WholesaleBusinessTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日订单金额")
    private Double todayOrderPrice;

    @Schema(description = "昨日UV")
    private Integer yesterdayUv;

    @Schema(description = "今日UV")
    private Integer todayUv;

    @Schema(description = "近7日UV")
    private Integer lastSevenUv;

    @Schema(description = "待分账金额")
    private Double pendingShareAmount;

    @Schema(description = "待采购入库单数")
    private Long pendingInboundCount;

    @Schema(description = "待审核店铺数")
    private Long submittedStoreCount;

    @Schema(description = "核销异常数")
    private Long verificationExceptionCount;
}
