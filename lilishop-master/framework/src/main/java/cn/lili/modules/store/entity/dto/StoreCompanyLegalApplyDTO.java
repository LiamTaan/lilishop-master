package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业法人入驻申请
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreCompanyLegalApplyDTO extends StoreApplySubmitDTO {

    @NotBlank(message = "法人姓名不能为空")
    @Schema(description = "法人姓名")
    private String legalName;

    @NotBlank(message = "法人身份证号不能为空")
    @Schema(description = "法人身份证号")
    private String legalId;
}
