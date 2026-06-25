package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台首页店铺卡片
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeStoreCardVO {

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "店铺 Logo")
    private String storeLogo;

    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "商品数量")
    private Integer goodsNum;

    @Schema(description = "收藏数量")
    private Integer collectionNum;

    @Schema(description = "来源规则 ORDER/COLLECTION/FOOTPRINT/HOT")
    private String source;
}
