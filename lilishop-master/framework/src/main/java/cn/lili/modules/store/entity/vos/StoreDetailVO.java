package cn.lili.modules.store.entity.vos;

import cn.lili.modules.store.entity.dto.StoreEditDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺详细VO
 *
 * @author pikachu
 * @since 2020-03-09 21:53:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreDetailVO extends StoreEditDTO {

    @Schema(description = "客户ID")
    private String memberId;

    @Schema(description = "客户名称")
    private String memberName;

    @Schema(description = "店铺状态")
    private String storeDisable;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "是否自营")
    private Boolean selfOperated;

    @Schema(description = "腾讯云智服唯一标识")
    private String yzfSign;

    @Schema(description = "腾讯云智服小程序唯一标识")
    private String yzfMpSign;

    @Schema(description = "udesk标识")
    private String merchantEuid;
}
