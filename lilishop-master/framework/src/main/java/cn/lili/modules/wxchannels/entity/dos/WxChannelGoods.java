package cn.lili.modules.wxchannels.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("li_wx_channel_goods")
@Schema(description = "微信视频号商品")
public class WxChannelGoods extends BaseEntity {

    @Schema(description = "平台商品ID，对应商城商品主键", example = "190001")
    private String goodsId;

    @Schema(description = "平台SKU ID，对应商城 SKU 主键", example = "290001")
    private String skuId;

    @Schema(description = "商品名称", example = "新疆吊干杏 500g")
    private String goodsName;

    @Schema(description = "商品主图 URL", example = "https://cdn.example.com/goods/xj-apricot.jpg")
    private String goodsImage;

    @Schema(description = "平台分类ID", example = "1001001")
    private String categoryId;

    @Schema(description = "平台分类名称", example = "休闲零食/坚果炒货")
    private String categoryName;

    @Schema(description = "店铺ID", example = "70001")
    private String storeId;

    @Schema(description = "店铺名称", example = "优选干货旗舰店")
    private String storeName;

    @Schema(description = "商城销售价，单位：元", example = "49.90")
    private Double costPrice;

    @Schema(description = "视频号渠道销售价，单位：元", example = "45.90")
    private Double channelPrice;

    @Schema(description = "可售库存", example = "128")
    private Integer stock;

    @Schema(
            description = "类目/商品审核状态：APPROVED=审核通过，PENDING=待审核，REJECTED=审核驳回",
            allowableValues = {"APPROVED", "PENDING", "REJECTED"},
            example = "APPROVED"
    )
    private String status;
}
