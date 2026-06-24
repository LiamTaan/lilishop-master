package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代理商采购对账视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentProcurementReconciliationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "采购单ID")
    private String id;

    @Schema(description = "采购单号")
    private String orderSn;

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

    @Schema(description = "审核时间")
    private Date auditTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "备注")
    private String remark;
}
