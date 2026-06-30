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

    @Schema(description = "兼容旧版的跳转值")
    private String url;

    @Schema(description = "跳转类型 CATEGORY/GOODS_DETAIL/COUPON/SPECIAL/ACTIVITY/CUSTOM_URL")
    private String linkType;

    @Schema(description = "跳转目标值")
    private String linkValue;
}
