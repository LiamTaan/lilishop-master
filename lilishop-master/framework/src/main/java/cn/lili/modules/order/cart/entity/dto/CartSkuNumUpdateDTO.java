package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车商品数量更新请求
 *
 * @author Codex
 */
@Data
public class CartSkuNumUpdateDTO implements Serializable {

    private static final long serialVersionUID = -8912493130947553232L;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品数量不能为空")
    @Min(value = 1, message = "产品数量必须大于0")
    private Integer num;
}
