package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * App 短信登录认证请求
 *
 * @author OpenAI
 */
@Data
public class AppLoginSmsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "手机号不能为空")
    @Schema(
            description = "用于登录或自动注册的手机号。若该手机号尚未注册，认证成功后系统会自动创建会员账号。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "13800138000"
    )
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(
            description = "登录短信验证码，需要与请求头中的 `uuid` 对应。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "123456"
    )
    private String code;
}
