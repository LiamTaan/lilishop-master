package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端保存店铺申请资料
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreAdminSaveDTO extends StoreEditDTO {

    @NotBlank(message = "客户ID不能为空")
    @Schema(description = "客户ID")
    private String memberId;
}
