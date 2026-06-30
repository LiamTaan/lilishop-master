package cn.lili.modules.goods.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端商品操作DTO
 *
 * @author Codex
 * @since 2026-06-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端商品操作DTO")
public class ManagerGoodsOperationDTO extends GoodsOperationDTO {

    private static final long serialVersionUID = -7955503690795093670L;

    @Schema(description = "归属店铺ID", required = true)
    @NotEmpty(message = "归属店铺不能为空")
    private String storeId;
}
