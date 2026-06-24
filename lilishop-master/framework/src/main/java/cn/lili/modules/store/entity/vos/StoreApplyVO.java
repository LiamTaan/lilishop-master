package cn.lili.modules.store.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺申请视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreApplyVO extends StoreDetailVO {

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "申请主体类型")
    private String applyType;

    @Schema(description = "店铺类型")
    private String storeType;
}
