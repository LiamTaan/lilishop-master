package cn.lili.modules.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建代理商身份请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentRoleCreateDTO {

    @NotBlank(message = "会员ID不能为空")
    @Schema(description = "会员ID")
    private String memberId;

    @NotBlank(message = "区域ID不能为空")
    @Schema(description = "区域ID")
    private String regionId;

    @NotBlank(message = "区域名称不能为空")
    @Schema(description = "区域名称")
    private String regionName;

    @NotBlank(message = "代理等级不能为空")
    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "备注")
    private String remark;
}
