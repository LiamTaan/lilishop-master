package cn.lili.modules.goods.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsVirtualSalesSingleDTO {

    @NotNull(message = "虚拟销量不能为空")
    @Min(value = 0, message = "虚拟销量不能小于0")
    @Max(value = 99999999, message = "虚拟销量不能超过99999999")
    @Schema(description = "虚拟销量")
    private Integer virtualSales;
}
