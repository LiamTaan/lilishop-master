package cn.lili.modules.member.entity.vo;

import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录会话快照
 *
 * @author OpenAI
 */
@Data
public class LoginSessionSnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
            description = "登录会话 token。前端需携带它继续调用身份查询或身份选择接口。默认有效期 10 分钟。",
            example = "LOGIN_SESSION196877654321"
    )
    private String loginSessionToken;

    @Schema(description = "当前手机号对应的会员ID", example = "191234567890123456")
    private String memberId;

    @Schema(description = "本次认证确认的手机号", example = "13800138000")
    private String mobile;

    @Schema(
            description = "创建该登录会话的认证渠道。`SMS` 表示短信验证码认证，`ONE_CLICK` 表示本机号码一键登录认证。",
            allowableValues = {"SMS", "ONE_CLICK"},
            example = "SMS"
    )
    private LoginSessionChannelEnum channel;

    @Schema(description = "当前账号可选择的身份列表。前端应根据 `available`、`status`、`nextAction` 决定是直接进入还是引导用户开通/重提/联系管理员。")
    private List<LoginIdentityOptionVO> identityOptions;
}
