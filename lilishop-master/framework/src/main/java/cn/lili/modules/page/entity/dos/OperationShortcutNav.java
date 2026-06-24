package cn.lili.modules.page.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页分类宫格配置
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_operation_shortcut_nav")
@Schema(description = "首页分类宫格配置")
public class OperationShortcutNav extends BaseEntity {

    private static final long serialVersionUID = 1L;

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

    @Schema(description = "客户端类型")
    private String clientType;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "显示状态 OPEN/CLOSE")
    private String displayStatus;

    @Schema(description = "备注")
    private String remark;
}
