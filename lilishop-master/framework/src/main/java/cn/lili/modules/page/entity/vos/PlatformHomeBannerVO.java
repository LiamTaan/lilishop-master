package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台首页 Banner
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeBannerVO {

    @Schema(description = "图片地址")
    private String image;

    @Schema(description = "跳转地址")
    private String url;
}
