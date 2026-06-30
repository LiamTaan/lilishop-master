package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 店铺结算资料
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
public class StoreSettlementProfileDTO {

    @Schema(description = "店铺ID")
    private String storeId;

    @NotBlank(message = "结算银行开户名不能为空")
    @Schema(description = "结算银行开户名")
    private String bankAccountName;

    @NotBlank(message = "结算银行账号不能为空")
    @Schema(description = "结算银行账号")
    private String bankAccountNumber;

    @NotBlank(message = "开户支行不能为空")
    @Schema(description = "开户支行")
    private String bankBranchName;

    @NotBlank(message = "联行号不能为空")
    @Schema(description = "联行号")
    private String bankJointCode;

    @NotBlank(message = "结算周期不能为空")
    @Schema(description = "结算周期")
    private String settlementCycle;

    @Schema(description = "结算日")
    private Date settlementDay;
}
