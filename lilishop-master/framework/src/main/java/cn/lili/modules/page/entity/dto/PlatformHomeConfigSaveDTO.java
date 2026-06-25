package cn.lili.modules.page.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 首页配置保存 DTO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页配置保存 DTO")
public class PlatformHomeConfigSaveDTO {

    @NotBlank
    @Schema(description = "客户端类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientType;

    @Schema(description = "页面名称")
    private String pageName;

    @Schema(description = "是否立即发布")
    private Boolean publishNow;

    @Schema(description = "轮播图列表")
    private List<PlatformHomeConfigBannerDTO> banners;

    @Schema(description = "顶部广告")
    private PlatformHomeConfigBannerDTO topAdvert;

    @Schema(description = "快捷入口列表")
    private List<PlatformHomeConfigShortcutDTO> shortcutNavList;

    @Schema(description = "首页楼层列表")
    private List<PlatformHomeConfigFloorDTO> floorModules;
}
