package cn.lili.modules.goods.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GoodsAuthUpdateDTO {

    @NotEmpty(message = "商品ID列表不能为空")
    @Schema(description = "商品ID列表")
    private List<String> goodsIds;

    @NotBlank(message = "审核状态不能为空")
    @Schema(description = "审核状态")
    private String authFlag;
}
