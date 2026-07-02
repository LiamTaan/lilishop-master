package cn.lili.modules.member.entity.vo;

import cn.lili.modules.member.entity.enums.LoginAuthDomainEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityNextActionEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录身份选项
 *
 * @author OpenAI
 */
@Data
public class LoginIdentityOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
            description = "身份编码。`CONSUMER` 消费者，`AGENT` 代理商，`SUPPLIER` 供货商。",
            allowableValues = {"CONSUMER", "AGENT", "SUPPLIER"},
            example = "CONSUMER"
    )
    private LoginIdentityCodeEnum identityCode;

    @Schema(
            description = "认证域。`MEMBER` 表示买家/会员域，`STORE` 表示供货商域。",
            allowableValues = {"MEMBER", "STORE"},
            example = "MEMBER"
    )
    private LoginAuthDomainEnum authDomain;

    @Schema(
            description = "是否允许当前会话直接进入该身份。`true` 可直接调用身份选择接口换取 token，`false` 需根据状态做引导。",
            example = "true"
    )
    private Boolean available;

    @Schema(
            description = "身份状态。`AVAILABLE` 可进入，`NOT_OPENED` 未开通，`PENDING` 审核中，`REJECTED` 审核拒绝，`DISABLED` 已禁用或未初始化，`CONFLICT` 表示当前账号已占用另一种互斥身份，不能同时开通。",
            allowableValues = {"AVAILABLE", "NOT_OPENED", "PENDING", "REJECTED", "DISABLED", "CONFLICT"},
            example = "AVAILABLE"
    )
    private LoginIdentityStatusEnum status;

    @Schema(
            description = "给前端展示的状态提示文案，可直接用于身份卡片上的辅助说明。",
            example = "可直接进入消费者身份"
    )
    private String message;

    @Schema(
            description = "当 `available=false` 时前端建议执行的后续动作。`NONE` 无需额外动作，`APPLY_AGENT` 申请代理商，`APPLY_STORE` 申请供货商，`RESUBMIT` 重新提交资料，`CONTACT_ADMIN` 联系管理员。",
            allowableValues = {"NONE", "APPLY_AGENT", "APPLY_STORE", "RESUBMIT", "CONTACT_ADMIN"},
            example = "NONE"
    )
    private LoginIdentityNextActionEnum nextAction;
}
