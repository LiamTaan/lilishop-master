package cn.lili.modules.wxchannels.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("li_wx_channel_category")
@Schema(description = "微信视频号类目申请")
public class WxChannelCategory extends BaseEntity {

    @Schema(description = "微信视频号三级类目 ID", example = "1234567")
    private String wxCategoryId;

    @Schema(description = "微信视频号三级类目名称", example = "坚果炒货")
    private String wxCategoryName;

    @Schema(description = "平台商品分类 ID", example = "1001001")
    private String platformCategoryId;

    @Schema(description = "平台商品分类名称", example = "休闲零食/坚果炒货")
    private String platformCategoryName;

    @Schema(
            description = "提审状态：APPROVED=审核通过，PENDING=待审核，REJECTED=审核驳回",
            allowableValues = {"APPROVED", "PENDING", "REJECTED"},
            example = "PENDING"
    )
    private String status;

    @Schema(
            description = "提审资质材料 JSON，通常包含类目资质、商品资质等微信要求的素材列表",
            example = "{\"qualification\":[{\"mediaId\":\"MEDIA_ID_1\",\"name\":\"食品经营许可证\"}]}"
    )
    private String materials;
}
