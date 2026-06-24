package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.dto.CardCouponGoodsBindDTO;
import cn.lili.modules.promotion.entity.dto.search.CardCouponGoodsSearchParams;
import cn.lili.modules.promotion.entity.vos.CardCouponGoodsVO;
import cn.lili.modules.promotion.service.CardCouponGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理端,卡券商品接口")
@RequestMapping("/manager/promotion/cardCouponGoods")
public class CardCouponGoodsManagerController {

    @Autowired
    private CardCouponGoodsService cardCouponGoodsService;

    @Operation(summary = "绑定卡券商品")
    @PostMapping("/bind")
    public ResultMessage<Object> bind(@RequestBody @Valid CardCouponGoodsBindDTO bindDTO) {
        cardCouponGoodsService.bind(bindDTO);
        return ResultUtil.success();
    }

    @Operation(summary = "分页获取卡券商品")
    @GetMapping
    public ResultMessage<IPage<CardCouponGoodsVO>> page(CardCouponGoodsSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(cardCouponGoodsService.page(searchParams, pageVO));
    }

    @Operation(summary = "删除卡券商品关联")
    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delete(@PathVariable String ids) {
        cardCouponGoodsService.delete(Arrays.asList(ids.split(",")));
        return ResultUtil.success();
    }
}
