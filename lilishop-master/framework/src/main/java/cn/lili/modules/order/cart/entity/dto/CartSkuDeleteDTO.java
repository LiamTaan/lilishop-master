package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车删除商品请求
 *
 * @author Codex
 */
@Data
public class CartSkuDeleteDTO implements Serializable {

    private static final long serialVersionUID = 7337123295704461728L;

    @Schema(description = "产品id数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品id不能为空")
    private String[] skuIds;
}
