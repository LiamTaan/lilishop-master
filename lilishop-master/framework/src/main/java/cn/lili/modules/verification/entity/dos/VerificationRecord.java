package cn.lili.modules.verification.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 核销记录
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_verification_record")
@Schema(description = "核销记录")
public class VerificationRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

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

    @Schema(description = "核销来源 BUYER/STORE/MANAGER")
    private String sourceType;

    @Schema(description = "核销结果 SUCCESS/FAIL")
    private String resultType;

    @Schema(description = "核销备注")
    private String remark;

    @Schema(description = "核销时间")
    private Date verifyTime;
}
