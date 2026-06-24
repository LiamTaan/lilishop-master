package cn.lili.modules.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 代理商店铺绑定审核请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentStoreBindAuditDTO {

    @NotBlank(message = "审核状态不能为空")
    @Schema(description = "审核状态：APPROVED/REJECTED")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;
}
