package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MemberGroupUsersUpdateDTO {

    @NotEmpty
    @Schema(description = "客户ID列表")
    private List<String> memberIds;
}
