package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberBenefitStateUpdateDTO {

    @NotBlank
    @Schema(description = "权益状态")
    private String state;
}
