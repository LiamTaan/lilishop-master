package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人主体入驻申请
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StorePersonalApplyDTO extends StoreApplySubmitDTO {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    private String idCardNo;
}
