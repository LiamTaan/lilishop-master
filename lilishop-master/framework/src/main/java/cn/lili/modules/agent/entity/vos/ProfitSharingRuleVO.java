package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台分账规则视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class ProfitSharingRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "品类ID")
    private String categoryId;

    @Schema(description = "分账比例")
    private Double ratio;

    @Schema(description = "状态")
    private String status;
}
