package cn.lili.modules.wallet.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理端钱包账户分页 VO
 */
@Data
@Schema(description = "管理端钱包账户分页 VO")
public class MemberWalletPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "钱包ID")
    private String id;

    @Schema(description = "会员ID")
    private String memberId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "余额")
    private Double balance;

    @Schema(description = "可提现金额")
    private Double withdrawableAmount;

    @Schema(description = "冻结金额")
    private Double frozenAmount;
}
