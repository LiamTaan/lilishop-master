package cn.lili.modules.goods.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GoodsFreightUpdateDTO {

    @NotEmpty(message = "商品ID列表不能为空")
    @Schema(description = "商品ID列表")
    private List<String> goodsId;

    @NotBlank(message = "运费模板ID不能为空")
    @Schema(description = "运费模板ID")
    private String templateId;
}
