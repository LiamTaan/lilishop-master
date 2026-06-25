package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台首页商品卡片
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeGoodsCardVO {

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "规格ID")
    private String skuId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String goodsImage;

    @Schema(description = "商品售价")
    private Double price;

    @Schema(description = "原价")
    private Double originalPrice;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "销量")
    private Integer salesNum;
}
