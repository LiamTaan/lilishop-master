package cn.lili.modules.wallet.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设置支付密码 DTO
 */
@Data
public class MemberWalletPasswordSetDTO {

    @NotBlank
    @Schema(description = "支付密码")
    private String password;

    @NotBlank
    @Schema(description = "短信验证码")
    private String code;
}
