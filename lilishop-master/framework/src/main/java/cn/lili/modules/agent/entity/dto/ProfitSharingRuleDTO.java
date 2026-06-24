package cn.lili.modules.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 平台分账规则请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class ProfitSharingRuleDTO {

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称")
    private String ruleName;

    @NotBlank(message = "角色类型不能为空")
    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "品类ID")
    private String categoryId;

    @NotNull(message = "分账比例不能为空")
    @PositiveOrZero(message = "分账比例不能小于0")
    @Schema(description = "分账比例")
    private Double ratio;

    @Schema(description = "状态")
    private String status;
}
