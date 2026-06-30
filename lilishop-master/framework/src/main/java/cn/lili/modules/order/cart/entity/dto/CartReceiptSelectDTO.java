package cn.lili.modules.order.cart.entity.dto;

import cn.lili.modules.order.order.entity.vo.ReceiptVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车选择发票请求
 *
 * @author Codex
 */
@Data
public class CartReceiptSelectDTO implements Serializable {

    private static final long serialVersionUID = 3983397003876359847L;

    @Schema(description = "购物车类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "购物车类型不能为空")
    private String way;

    @Schema(description = "发票信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发票信息不能为空")
    @Valid
    private ReceiptVO receipt;
}
