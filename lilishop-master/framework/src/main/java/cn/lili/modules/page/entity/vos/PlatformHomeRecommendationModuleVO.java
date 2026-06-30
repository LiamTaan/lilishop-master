package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页推荐模块聚合 VO
 *
 * @author codex
 * @since 2026/6/26
 */
@Data
@Schema(description = "首页推荐模块聚合 VO")
public class PlatformHomeRecommendationModuleVO {

    @Schema(description = "模块编码")
    private String code;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "模块排序")
    private Integer sortOrder;

    @Schema(description = "模块类型 STORE/GOODS")
    private String moduleType;

    @Schema(description = "店铺结果列表")
    private List<PlatformHomeStoreCardVO> storeList;

    @Schema(description = "商品结果列表")
    private List<PlatformHomeGoodsCardVO> goodsList;
}
