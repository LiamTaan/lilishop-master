package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台分账记录视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class ProfitSharingRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "账单ID")
    private String id;

    @Schema(description = "账单号")
    private String sn;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "账单状态")
    private String billStatus;

    @Schema(description = "应结金额")
    private Double billPrice;

    @Schema(description = "退款金额")
    private Double refundPrice;

    @Schema(description = "分账金额")
    private Double commissionAmount;

    @Schema(description = "结算状态")
    private String settlementStatus;

    @Schema(description = "创建时间")
    private Date createTime;
}
