package cn.lili.modules.order.order.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 交易投诉沟通 DTO
 */
@Data
public class OrderComplaintCommunicationDTO {

    @NotBlank
    @Schema(description = "投诉单ID")
    private String complainId;

    @NotBlank
    @Schema(description = "沟通内容")
    private String content;
}
