package cn.lili.modules.promotion.entity.vos;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.modules.promotion.entity.dos.Coupon;
import cn.lili.modules.promotion.entity.dos.PromotionGoods;
import cn.lili.modules.promotion.entity.enums.CouponTypeEnum;
import cn.lili.modules.promotion.entity.enums.PromotionsStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券视图对象
 *
 * @author Chopper
 * @since 2020/8/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "优惠券")
@ToString(callSuper = true)
@NoArgsConstructor
public class CouponVO extends Coupon {

    private static final long serialVersionUID = 8372420376262437018L;

    /**
     * 促销关联的商品
     */
    @Schema(description = "优惠券关联商品集合")
    private List<PromotionGoods> promotionGoodsList;

    @Schema(description = "优惠券类型文案")
    private String couponTypeLabel;

    @Schema(description = "面值展示文案")
    private String faceValueText;

    @Schema(description = "可用库存，0 表示已领完；当 unlimitedStock=true 时不受该值约束")
    private Integer stockNum;

    @Schema(description = "是否不限量")
    private Boolean unlimitedStock;

    @Schema(description = "库存展示文案")
    private String stockText;

    @Schema(description = "促销状态文案")
    private String promotionStatusLabel;

    public CouponVO(Coupon coupon) {
        if (coupon == null) {
            return;
        }
        BeanUtils.copyProperties(coupon, this);
    }

    public String getCouponTypeLabel() {
        if (CharSequenceUtil.isBlank(couponTypeLabel)) {
            couponTypeLabel = resolveCouponTypeLabel();
        }
        return couponTypeLabel;
    }

    public String getFaceValueText() {
        if (CharSequenceUtil.isBlank(faceValueText)) {
            faceValueText = resolveFaceValueText();
        }
        return faceValueText;
    }

    public Integer getStockNum() {
        if (stockNum == null) {
            stockNum = resolveStockNum();
        }
        return stockNum;
    }

    public Boolean getUnlimitedStock() {
        if (unlimitedStock == null) {
            Integer publishNum = getPublishNum();
            unlimitedStock = publishNum != null && publishNum == 0;
        }
        return unlimitedStock;
    }

    public String getStockText() {
        if (CharSequenceUtil.isBlank(stockText)) {
            stockText = Boolean.TRUE.equals(getUnlimitedStock()) ? "不限量" : String.valueOf(getStockNum());
        }
        return stockText;
    }

    public String getPromotionStatusLabel() {
        if (CharSequenceUtil.isBlank(promotionStatusLabel)) {
            promotionStatusLabel = resolvePromotionStatusLabel();
        }
        return promotionStatusLabel;
    }

    private String resolveCouponTypeLabel() {
        if (CharSequenceUtil.isBlank(getCouponType())) {
            return null;
        }
        try {
            return CouponTypeEnum.valueOf(getCouponType()).description();
        } catch (IllegalArgumentException ignored) {
            return getCouponType();
        }
    }

    private String resolveFaceValueText() {
        if (CharSequenceUtil.isBlank(getCouponType())) {
            return formatDecimal(getPrice());
        }
        try {
            CouponTypeEnum couponTypeEnum = CouponTypeEnum.valueOf(getCouponType());
            return switch (couponTypeEnum) {
                case DISCOUNT -> formatDecimal(getCouponDiscount()) + "折";
                case PRICE -> "¥" + formatDecimal(getPrice());
            };
        } catch (IllegalArgumentException ignored) {
            return formatDecimal(getPrice());
        }
    }

    private Integer resolveStockNum() {
        Integer publishNum = getPublishNum();
        if (publishNum == null) {
            return 0;
        }
        if (publishNum == 0) {
            return 0;
        }
        Integer receivedNum = getReceivedNum();
        int received = receivedNum == null ? 0 : receivedNum;
        return Math.max(publishNum - received, 0);
    }

    private String resolvePromotionStatusLabel() {
        String status = getPromotionStatus();
        if (CharSequenceUtil.isBlank(status)) {
            return null;
        }
        try {
            return PromotionsStatusEnum.valueOf(status).description();
        } catch (IllegalArgumentException ignored) {
            return status;
        }
    }

    private String formatDecimal(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
