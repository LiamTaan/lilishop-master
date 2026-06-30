package cn.lili.modules.order.order.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderSellerRemarkDTO {

    @NotBlank(message = "卖家备注不能为空")
    @Schema(description = "卖家备注")
    private String sellerRemark;
}
