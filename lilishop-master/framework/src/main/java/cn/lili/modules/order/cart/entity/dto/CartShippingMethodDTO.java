package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车选择配送方式请求
 *
 * @author Codex
 */
@Data
public class CartShippingMethodDTO implements Serializable {

    private static final long serialVersionUID = 5473724477217498010L;

    @Schema(description = "配送方式：SELF_PICK_UP / LOCAL_TOWN_DELIVERY / LOGISTICS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配送方式不能为空")
    private String shippingMethod;

    @Schema(description = "购物车类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "购物车类型不能为空")
    private String way;
}
