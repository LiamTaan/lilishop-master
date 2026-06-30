package cn.lili.modules.order.order.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPriceUpdateDTO {

    @NotNull(message = "订单价格不能为空")
    @Schema(description = "订单价格")
    private Double price;
}
