package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 客户状态修改 DTO
 */
@Data
public class MemberStatusUpdateDTO {

    @NotEmpty
    @Schema(description = "客户ID列表")
    private List<String> memberIds;

    @NotNull
    @Schema(description = "是否禁用")
    private Boolean disabled;
}
