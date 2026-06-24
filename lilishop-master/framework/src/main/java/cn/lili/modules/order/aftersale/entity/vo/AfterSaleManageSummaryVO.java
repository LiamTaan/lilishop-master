package cn.lili.modules.order.aftersale.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 售后治理汇总
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AfterSaleManageSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总数")
    private Long totalCount;

    @Schema(description = "申请中")
    private Long applyCount;

    @Schema(description = "已通过")
    private Long passCount;

    @Schema(description = "已拒绝")
    private Long refuseCount;

    @Schema(description = "待卖家收货")
    private Long buyerReturnCount;

    @Schema(description = "等待平台退款")
    private Long waitRefundCount;

    @Schema(description = "已完成")
    private Long completeCount;
}
