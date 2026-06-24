package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.vos.CardCouponGoodsVO;
import cn.lili.modules.promotion.service.CardCouponGoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端,卡券商品接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "买家端,卡券商品接口")
@RequestMapping("/buyer/promotion/cardCouponGoods")
public class CardCouponGoodsBuyerController {

    @Autowired
    private CardCouponGoodsService cardCouponGoodsService;

    @Operation(summary = "根据优惠券获取卡券商品")
    @GetMapping("/coupon/{couponId}")
    public ResultMessage<List<CardCouponGoodsVO>> listByCouponId(@PathVariable String couponId) {
        return ResultUtil.data(cardCouponGoodsService.listByCouponId(couponId));
    }

    @Operation(summary = "根据SKU获取可用卡券")
    @GetMapping("/sku/{skuId}")
    public ResultMessage<List<CardCouponGoodsVO>> listBySkuId(@PathVariable String skuId) {
        return ResultUtil.data(cardCouponGoodsService.listBySkuId(skuId));
    }
}
