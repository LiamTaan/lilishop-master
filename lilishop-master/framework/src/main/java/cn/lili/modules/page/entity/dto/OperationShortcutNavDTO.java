package cn.lili.modules.page.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页分类配置查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@Schema(description = "首页分类配置查询参数")
public class OperationShortcutNavDTO {

    @Schema(description = "客户端类型")
    private String clientType;

    @Schema(description = "显示状态")
    private String displayStatus;

    @Schema(description = "标题")
    private String title;
}
