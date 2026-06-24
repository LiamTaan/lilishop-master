package cn.lili.modules.member.entity.dto;

import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 身份选择请求
 *
 * @author OpenAI
 */
@Data
public class LoginIdentitySelectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录会话不能为空")
    @Schema(
            description = "短信认证或一键登录成功后返回的登录会话 token。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "LOGIN_SESSION196877654321"
    )
    private String loginSessionToken;

    @NotNull(message = "身份编码不能为空")
    @Schema(
            description = "要进入的身份编码。消费者端通常选择 `CONSUMER` 或 `AGENT`，供货商端选择 `SUPPLIER`。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CONSUMER", "AGENT", "SUPPLIER"},
            example = "CONSUMER"
    )
    private LoginIdentityCodeEnum identityCode;
}
