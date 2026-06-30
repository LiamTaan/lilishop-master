package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页推荐模块配置 VO
 *
 * @author codex
 * @since 2026/6/26
 */
@Data
@Schema(description = "首页推荐模块配置 VO")
public class PlatformHomeRecommendationConfigVO {

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "展示数量")
    private Integer limit;

    @Schema(description = "分类范围")
    private List<String> categoryRange;

    @Schema(description = "是否优先推荐商品标记")
    private Boolean preferRecommendFlag;

    @Schema(description = "冷启动策略 HOT_AND_NEW/HOT_ONLY")
    private String coldStartStrategy;

    @Schema(description = "价格上限")
    private Double priceUpperBound;

    @Schema(description = "活动类型范围")
    private List<String> promotionTypes;
}
