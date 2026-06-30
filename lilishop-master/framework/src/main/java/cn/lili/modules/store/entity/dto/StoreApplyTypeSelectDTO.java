package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 选择店铺入驻主体类型
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
public class StoreApplyTypeSelectDTO {

    @NotBlank(message = "主体类型不能为空")
    @Schema(description = "主体类型：PERSONAL/INDIVIDUAL/COMPANY")
    private String subjectType;

    @Schema(description = "店铺类型")
    private String storeType;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "代理区域ID")
    private String agentRegionId;

    @Schema(description = "代理区域名称")
    private String agentRegionName;
}
