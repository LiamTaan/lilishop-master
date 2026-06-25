package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台首页快捷入口
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeShortcutNavVO {

    @Schema(description = "入口标题")
    private String title;

    @Schema(description = "入口副标题")
    private String subtitle;

    @Schema(description = "图标")
    private String image;

    @Schema(description = "跳转类型")
    private String linkType;

    @Schema(description = "跳转值")
    private String linkValue;
}
