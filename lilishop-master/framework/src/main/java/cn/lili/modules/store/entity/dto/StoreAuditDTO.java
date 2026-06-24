package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 店铺审核请求
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class StoreAuditDTO {

    @NotBlank(message = "审核状态不能为空")
    @Schema(description = "审核状态：APPROVED/REJECTED/FROZEN")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;
}
