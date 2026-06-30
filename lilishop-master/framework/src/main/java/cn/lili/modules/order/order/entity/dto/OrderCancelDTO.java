package cn.lili.modules.order.order.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancelDTO {

    @NotBlank(message = "取消原因不能为空")
    @Schema(description = "取消原因")
    private String reason;
}
