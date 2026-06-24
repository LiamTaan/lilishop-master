package cn.lili.modules.wallet.entity.dto;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 管理端钱包账户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端钱包账户查询参数")
public class MemberWalletSearchParams extends PageVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "手机号")
    private String mobile;
}
