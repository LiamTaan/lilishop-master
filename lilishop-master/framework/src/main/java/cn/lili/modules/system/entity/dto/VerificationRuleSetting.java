package cn.lili.modules.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 核销规则设置
 *
 * @author Codex
 * @since 2026/6/19
 */
@Data
public class VerificationRuleSetting implements Serializable {

    private static final long serialVersionUID = 3335422068176730027L;

    @Schema(description = "是否启用自提核销链")
    private Boolean pickupEnabled = Boolean.TRUE;

    @Schema(description = "是否启用虚拟核验链")
    private Boolean virtualVerificationEnabled = Boolean.TRUE;

    @Schema(description = "买家是否显示立即核销入口")
    private Boolean buyerShowVerificationButton = Boolean.TRUE;

    @Schema(description = "买家详情是否展示取件码")
    private Boolean buyerShowVerificationCode = Boolean.TRUE;

    @Schema(description = "核销码长度")
    private Integer verificationCodeLength = 12;

    @Schema(description = "平台规则说明")
    private String ruleRemark = "";
}
