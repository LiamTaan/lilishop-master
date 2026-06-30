package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车选择收货地址请求
 *
 * @author Codex
 */
@Data
public class CartShippingAddressDTO implements Serializable {

    private static final long serialVersionUID = -5649582540198938569L;

    @Schema(description = "收货地址id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货地址ID不能为空")
    private String shippingAddressId;

    @Schema(description = "购物车类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "购物车类型不能为空")
    private String way;
}
