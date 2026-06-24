package cn.lili.modules.promotion.entity.dto.search;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 卡券商品关联查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@Schema(description = "卡券商品关联查询参数")
public class CardCouponGoodsSearchParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    private String couponId;

    @Schema(description = "商品SKU ID")
    private String skuId;

    @Schema(description = "商品名称")
    private String goodsName;

    public <T> QueryWrapper<T> queryWrapper() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(couponId)) {
            queryWrapper.eq("coupon_id", couponId);
        }
        if (CharSequenceUtil.isNotBlank(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        if (CharSequenceUtil.isNotBlank(goodsName)) {
            queryWrapper.like("goods_name", goodsName);
        }
        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }
}
