package cn.lili.modules.goods.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺端扫码发布结果
 *
 * @author dawn
 * @since 2026/6/22
 */
@Data
public class StoreGoodsScanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "扫描条码")
    private String barcode;

    @Schema(description = "是否命中现有商品")
    private Boolean matched;

    @Schema(description = "命中类型：NONE、GOODS、SKU")
    private String matchType;

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品主条码")
    private String goodsBarcode;

    @Schema(description = "规格ID")
    private String skuId;

    @Schema(description = "规格名称")
    private String skuName;

    @Schema(description = "规格条码")
    private String skuBarcode;

    @Schema(description = "规格简短信息")
    private String simpleSpecs;
}
