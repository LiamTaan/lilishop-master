package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺入驻提交公共字段
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class StoreApplySubmitDTO extends StoreApplyCommonDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String smsCode;

    @AssertTrue(message = "请先阅读并同意协议")
    @Schema(description = "是否已同意协议")
    private Boolean agreementAccepted;
}
