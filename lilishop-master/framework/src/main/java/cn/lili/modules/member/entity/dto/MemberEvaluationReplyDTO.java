package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberEvaluationReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容")
    private String reply;

    @Schema(description = "回复图片")
    private String replyImage;
}
