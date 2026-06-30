package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 平台首页聚合数据
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeVO {

    @Schema(description = "客户端类型")
    private String clientType;

    @Schema(description = "Banner 列表")
    private List<PlatformHomeBannerVO> banners;

    @Schema(description = "首页快捷入口")
    private List<PlatformHomeShortcutNavVO> shortcutNavList;

    @Schema(description = "首页楼层模块")
    private List<PlatformHomeFloorModuleVO> floorModules;

    @Schema(description = "秒杀模块")
    private PlatformHomeSeckillVO seckill;

    @Schema(description = "本月热销")
    private List<PlatformHomeGoodsCardVO> monthlyHotGoods;

    @Schema(description = "上新商品")
    private List<PlatformHomeGoodsCardVO> newGoods;

    @Schema(description = "常用店铺")
    private List<PlatformHomeStoreCardVO> frequentStores;

    @Schema(description = "猜你喜欢商品")
    private List<PlatformHomeGoodsCardVO> guessYouLikeGoods;

    @Schema(description = "低价专区商品")
    private List<PlatformHomeGoodsCardVO> lowPriceZoneGoods;

    @Schema(description = "今日特惠商品")
    private List<PlatformHomeGoodsCardVO> todaySpecialGoods;

    @Schema(description = "推荐模块聚合列表")
    private List<PlatformHomeRecommendationModuleVO> recommendationModules;
}
