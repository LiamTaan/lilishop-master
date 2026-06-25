package cn.lili.modules.page.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页配置 Banner DTO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页配置 Banner DTO")
public class PlatformHomeConfigBannerDTO {

    @Schema(description = "图片地址")
    private String image;

    @Schema(description = "跳转地址")
    private String url;
}
