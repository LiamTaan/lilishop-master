package cn.lili.modules.promotion.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 卡券商品关联
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_card_coupon_goods")
@Schema(description = "卡券商品关联")
public class CardCouponGoods extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    private String couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品SKU ID")
    private String skuId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品缩略图")
    private String thumbnail;

    @Schema(description = "商品原价")
    private Double originalPrice;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;
}
