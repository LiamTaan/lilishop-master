package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户积分修改 DTO
 */
@Data
public class MemberPointUpdateDTO {

    @NotBlank
    @Schema(description = "客户ID")
    private String memberId;

    @NotNull
    @Schema(description = "积分变动值")
    private Long point;

    @NotBlank
    @Schema(description = "变动类型")
    private String type;
}
