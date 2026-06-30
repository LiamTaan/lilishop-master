package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.dto.CardCouponGoodsBindDTO;
import cn.lili.modules.promotion.entity.dto.search.CardCouponGoodsSearchParams;
import cn.lili.modules.promotion.entity.vos.CardCouponGoodsVO;
import cn.lili.modules.promotion.service.CardCouponGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 管理端,卡券商品接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@RequestMapping("/manager/promotion/cardCouponGoods")
public class CardCouponGoodsManagerController {

    @Autowired
    private CardCouponGoodsService cardCouponGoodsService;

    @PostMapping("/bind")
    public ResultMessage<Object> bind(@RequestBody @Valid CardCouponGoodsBindDTO bindDTO) {
        cardCouponGoodsService.bind(bindDTO);
        return ResultUtil.success();
    }

    @GetMapping
    public ResultMessage<IPage<CardCouponGoodsVO>> page(CardCouponGoodsSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(cardCouponGoodsService.page(searchParams, pageVO));
    }

    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delete(@PathVariable String ids) {
        cardCouponGoodsService.delete(Arrays.asList(ids.split(",")));
        return ResultUtil.success();
    }
}
