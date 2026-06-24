package cn.lili.modules.wxchannels.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "单个视频号类目提审项")
public class WxChannelCategorySubmitItemDTO {

    @Schema(description = "微信视频号三级类目 ID", example = "1234567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String wxCategoryId;

    @Schema(description = "微信视频号三级类目名称", example = "坚果炒货", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String wxCategoryName;

    @Schema(description = "平台商品分类 ID", example = "1001001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String platformCategoryId;

    @Schema(description = "平台商品分类名称", example = "休闲零食/坚果炒货", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String platformCategoryName;

    @Schema(
            description = "资质材料 JSON，按微信类目要求上传对应资质素材",
            example = "{\"qualification\":[{\"mediaId\":\"MEDIA_ID_1\",\"name\":\"食品经营许可证\"}]}"
    )
    private String materials;
}
