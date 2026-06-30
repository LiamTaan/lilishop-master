package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MemberUserGroupsUpdateDTO {

    @NotEmpty
    @Schema(description = "分组ID列表")
    private List<String> groupIds;
}
