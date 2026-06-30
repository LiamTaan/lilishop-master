package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业被授权人入驻申请
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreCompanyAuthorizedApplyDTO extends StoreApplySubmitDTO {

    @NotBlank(message = "被授权人姓名不能为空")
    @Schema(description = "被授权人姓名")
    private String authorizedName;

    @NotBlank(message = "被授权人身份证号不能为空")
    @Schema(description = "被授权人身份证号")
    private String authorizedIdNo;

    @NotBlank(message = "授权书不能为空")
    @Schema(description = "授权书")
    private String authorizationUrl;
}
