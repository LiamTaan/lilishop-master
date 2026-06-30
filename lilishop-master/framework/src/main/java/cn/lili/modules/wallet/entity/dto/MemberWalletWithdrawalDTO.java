package cn.lili.modules.wallet.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户提现请求 DTO
 */
@Data
public class MemberWalletWithdrawalDTO {

    @NotNull
    @Max(value = 9999, message = "充值金额单次最多允许提现9999元")
    @Schema(description = "提现金额")
    private Double price;

    @NotBlank
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank
    @Schema(description = "第三方登录账号")
    private String connectNumber;
}
