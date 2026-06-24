package cn.lili.modules.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 代理商店铺绑定请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentStoreBindDTO {

    @NotBlank(message = "代理商会员ID不能为空")
    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @NotBlank(message = "店铺ID不能为空")
    @Schema(description = "店铺ID")
    private String storeId;

    @NotBlank(message = "区域ID不能为空")
    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "业务备注")
    private String remark;
}
