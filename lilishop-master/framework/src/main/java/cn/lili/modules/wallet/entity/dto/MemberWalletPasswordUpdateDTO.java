package cn.lili.modules.wallet.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改支付密码 DTO
 */
@Data
public class MemberWalletPasswordUpdateDTO {

    @NotBlank
    @Pattern(regexp = "[a-fA-F0-9]{32}", message = "旧密码格式不正确")
    @Schema(description = "旧支付密码")
    private String oldPassword;

    @NotBlank
    @Pattern(regexp = "[a-fA-F0-9]{32}", message = "新密码格式不正确")
    @Schema(description = "新支付密码")
    private String newPassword;
}
