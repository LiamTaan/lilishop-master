package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 加入购物车请求参数
 *
 * @author Codex
 */
@Data
public class AddCartDTO implements Serializable {

    private static final long serialVersionUID = -3549348479324745518L;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "产品id不能为空")
    private String skuId;

    @Schema(description = "此产品的购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "加入购物车数量必须大于0")
    private Integer num;

    @Schema(description = "购物车类型，默认加入购物车")
    private String cartType;
}
