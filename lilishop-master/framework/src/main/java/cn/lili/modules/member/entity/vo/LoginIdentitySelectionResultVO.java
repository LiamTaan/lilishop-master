package cn.lili.modules.member.entity.vo;

import cn.lili.common.security.token.Token;
import cn.lili.modules.member.entity.enums.LoginAuthDomainEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 身份确认登录结果
 *
 * @author OpenAI
 */
@Data
public class LoginIdentitySelectionResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
            description = "本次成功进入的身份编码。",
            allowableValues = {"CONSUMER", "AGENT", "SUPPLIER"},
            example = "CONSUMER"
    )
    private LoginIdentityCodeEnum identityCode;

    @Schema(
            description = "签发 token 所属认证域。`MEMBER` 对应买家端接口，`STORE` 对应供货商端接口。",
            allowableValues = {"MEMBER", "STORE"},
            example = "MEMBER"
    )
    private LoginAuthDomainEnum authDomain;

    @Schema(description = "最终登录 token。前端后续访问业务接口时应携带这里返回的 access token / refresh token。")
    private Token token;

    @Schema(description = "当前会员基础资料，可用于登录后初始化用户上下文。")
    private MemberVO memberProfile;

    @Schema(description = "签发 token 时重新计算的全量身份状态快照，便于前端同步展示其他身份的可用性。")
    private LoginIdentitySummaryVO identityStatusSummary;
}
