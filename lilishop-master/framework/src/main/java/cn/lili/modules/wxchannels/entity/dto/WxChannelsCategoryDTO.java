package cn.lili.modules.wxchannels.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信视频号三级类目")
public class WxChannelsCategoryDTO {

    @Schema(description = "三级类目 ID", example = "1234567")
    private Long thirdCatId;

    @Schema(description = "三级类目名称", example = "坚果炒货")
    private String thirdCatName;

    @Schema(description = "类目资质说明或素材要求")
    private String qualification;

    @Schema(description = "类目资质类型：0=不需要，1=必填，2=选填", allowableValues = {"0", "1", "2"}, example = "1")
    private Integer qualificationType;

    @Schema(description = "商品资质说明或素材要求")
    private String productQualification;

    @Schema(description = "商品资质类型：0=不需要，1=必填，2=选填", allowableValues = {"0", "1", "2"}, example = "2")
    private Integer productQualificationType;

    @Schema(description = "二级类目 ID", example = "223344")
    private Long secondCatId;

    @Schema(description = "二级类目名称", example = "休闲零食")
    private String secondCatName;

    @Schema(description = "一级类目 ID", example = "110022")
    private Long firstCatId;

    @Schema(description = "一级类目名称", example = "食品饮料")
    private String firstCatName;
}
