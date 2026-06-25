package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页配置聚合 VO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页配置聚合 VO")
public class PlatformHomeConfigVO {

    @Schema(description = "客户端类型")
    private String clientType;

    @Schema(description = "页面ID")
    private String pageId;

    @Schema(description = "页面名称")
    private String pageName;

    @Schema(description = "当前发布状态 OPEN/CLOSE")
    private String publishStatus;

    @Schema(description = "轮播图列表")
    private List<PlatformHomeBannerVO> banners;

    @Schema(description = "顶部广告")
    private PlatformHomeBannerVO topAdvert;

    @Schema(description = "快捷入口列表")
    private List<PlatformHomeConfigShortcutVO> shortcutNavList;

    @Schema(description = "首页楼层列表")
    private List<PlatformHomeFloorModuleVO> floorModules;

    @Schema(description = "规则型模块说明")
    private List<PlatformHomeRuleBlockVO> ruleBlocks;

    @Schema(description = "仍保留的遗留 section 数量")
    private Integer legacySectionCount;
}
