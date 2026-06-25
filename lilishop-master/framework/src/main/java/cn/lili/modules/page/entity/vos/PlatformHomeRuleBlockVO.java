package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页规则模块说明 VO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页规则模块说明 VO")
public class PlatformHomeRuleBlockVO {

    @Schema(description = "模块编码")
    private String code;

    @Schema(description = "模块名称")
    private String title;

    @Schema(description = "维护说明")
    private String description;

    @Schema(description = "来源菜单")
    private String sourceMenu;

    @Schema(description = "来源接口")
    private String sourceApi;
}
