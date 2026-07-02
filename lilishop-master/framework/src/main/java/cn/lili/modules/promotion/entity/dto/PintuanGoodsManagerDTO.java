package cn.lili.modules.promotion.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端添加拼团商品入参。
 */
@Data
@Schema(description = "管理端添加拼团商品入参")
public class PintuanGoodsManagerDTO implements Serializable {

    private static final long serialVersionUID = 1872667656799548236L;

    @Schema(description = "商品SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品SKU ID不能为空")
    private String skuId;

    @Schema(description = "拼团价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拼团价格不能为空")
    @DecimalMin(value = "0.0", message = "拼团价格不能小于0")
    private Double price;

    @Schema(description = "活动库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动库存不能为空")
    @Min(value = 0, message = "活动库存不能小于0")
    private Integer quantity;

    @Schema(description = "单品限购")
    @Min(value = 0, message = "单品限购不能小于0")
    private Integer limitNum;
}
