package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * App 一键登录认证请求
 *
 * @author OpenAI
 */
@Data
public class AppLoginOneClickDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "一键登录凭证不能为空")
    @Schema(
            description = "运营商或服务商返回的一键登录临时凭证。后端会基于该凭证换取手机号。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "eyJhbGciOiJIUzI1NiJ9.app-login-token"
    )
    private String loginToken;
}
