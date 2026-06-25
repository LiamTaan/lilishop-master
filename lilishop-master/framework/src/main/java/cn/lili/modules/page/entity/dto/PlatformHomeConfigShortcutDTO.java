package cn.lili.modules.page.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页配置快捷入口 DTO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页配置快捷入口 DTO")
public class PlatformHomeConfigShortcutDTO {

    @Schema(description = "快捷入口ID")
    private String id;

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

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "显示状态 OPEN/CLOSE")
    private String displayStatus;

    @Schema(description = "备注")
    private String remark;
}
