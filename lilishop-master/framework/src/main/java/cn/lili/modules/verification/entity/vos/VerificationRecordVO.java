package cn.lili.modules.verification.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 核销记录视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class VerificationRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核销记录ID")
    private String id;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "会员ID")
    private String memberId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "核销码")
    private String verificationCode;

    @Schema(description = "核销人ID")
    private String operatorId;

    @Schema(description = "核销人名称")
    private String operatorName;

    @Schema(description = "核销来源")
    private String sourceType;

    @Schema(description = "核销结果")
    private String resultType;

    @Schema(description = "核销备注")
    private String remark;

    @Schema(description = "核销时间")
    private Date verifyTime;

    @Schema(description = "创建时间")
    private Date createTime;
}
