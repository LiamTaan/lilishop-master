package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台采购对账视图
 *
 * @author dawn
 * @since 2026/6/19
 */
@Data
public class PlatformProcurementReconciliationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页面记录ID")
    private String id;

    @Schema(description = "采购单ID")
    private String orderId;

    @Schema(description = "采购单号")
    private String orderSn;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "代理商名称")
    private String agentName;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "采购总金额")
    private Double totalAmount;

    @Schema(description = "采购总数量")
    private Integer totalQuantity;

    @Schema(description = "采购状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;
}
