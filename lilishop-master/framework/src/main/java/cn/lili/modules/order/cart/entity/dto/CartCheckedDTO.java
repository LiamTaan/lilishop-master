package cn.lili.modules.order.cart.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车勾选状态请求
 *
 * @author Codex
 */
@Data
public class CartCheckedDTO implements Serializable {

    private static final long serialVersionUID = -8625539941700898154L;

    @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否选中不能为空")
    private Boolean checked;
}
