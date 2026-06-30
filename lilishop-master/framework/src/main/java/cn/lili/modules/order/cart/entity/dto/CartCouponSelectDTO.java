package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车选择优惠券请求
 *
 * @author Codex
 */
@Data
public class CartCouponSelectDTO implements Serializable {

    private static final long serialVersionUID = 7279813985890291509L;

    @Schema(description = "购物车类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "购物车类型不能为空")
    private String way;

    @Schema(description = "优惠券id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "优惠券id不能为空")
    private String memberCouponId;

    @Schema(description = "使用 true，弃用 false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否使用不能为空")
    private Boolean used;
}
