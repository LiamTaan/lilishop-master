package cn.lili.modules.promotion.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 卡券商品绑定请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@Schema(description = "卡券商品绑定请求")
public class CardCouponGoodsBindDTO {

    @NotBlank(message = "优惠券ID不能为空")
    @Schema(description = "优惠券ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponId;

    @NotEmpty(message = "SKU列表不能为空")
    @Schema(description = "商品SKU ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> skuIds;
}
