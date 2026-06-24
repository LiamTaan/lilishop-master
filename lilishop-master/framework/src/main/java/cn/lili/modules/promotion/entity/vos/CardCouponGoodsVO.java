package cn.lili.modules.promotion.entity.vos;

import cn.lili.modules.promotion.entity.dos.CardCouponGoods;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 卡券商品关联视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "卡券商品关联视图")
public class CardCouponGoodsVO extends CardCouponGoods {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券详情")
    private CouponVO coupon;
}
