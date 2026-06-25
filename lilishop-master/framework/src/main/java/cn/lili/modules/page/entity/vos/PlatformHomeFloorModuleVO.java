package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页楼层模块 VO
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
@Schema(description = "首页楼层模块 VO")
public class PlatformHomeFloorModuleVO {

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "模块副标题")
    private String subtitle;

    @Schema(description = "模块图片")
    private String image;

    @Schema(description = "模块类型 ACTIVITY_SLOT/RECOMMEND_FLOOR")
    private String moduleType;

    @Schema(description = "数据来源 SPECIAL/SECKILL/HOT_GOODS/NEW_GOODS/CUSTOM")
    private String sourceType;

    @Schema(description = "跳转类型")
    private String linkType;

    @Schema(description = "跳转值")
    private String linkValue;

    @Schema(description = "展示数量")
    private Integer goodsLimit;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "显示状态 OPEN/CLOSE")
    private String displayStatus;

    @Schema(description = "专题ID")
    private String specialId;

    @Schema(description = "专题名称")
    private String specialName;

    @Schema(description = "备注")
    private String remark;
}
