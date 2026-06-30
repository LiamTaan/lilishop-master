package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车选择自提地址请求
 *
 * @author Codex
 */
@Data
public class CartStoreAddressDTO implements Serializable {

    private static final long serialVersionUID = -6889029631829285790L;

    @Schema(description = "自提地址id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "自提地址ID不能为空")
    private String storeAddressId;

    @Schema(description = "购物车类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "购物车类型不能为空")
    private String way;
}
