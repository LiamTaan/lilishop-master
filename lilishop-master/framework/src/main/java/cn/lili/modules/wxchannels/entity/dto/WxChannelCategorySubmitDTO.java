package cn.lili.modules.wxchannels.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量提审视频号类目请求")
public class WxChannelCategorySubmitDTO {

    @Schema(description = "类目提审项列表，至少传 1 条", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    private List<WxChannelCategorySubmitItemDTO> items;
}
