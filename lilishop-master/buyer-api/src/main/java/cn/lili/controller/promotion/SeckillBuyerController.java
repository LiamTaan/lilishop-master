package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.vos.SeckillGoodsVO;
import cn.lili.modules.promotion.entity.vos.SeckillTimelineVO;
import cn.lili.modules.promotion.service.SeckillApplyService;
import cn.lili.modules.store.support.BuyerStoreScopeSupport;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * 买家端,秒杀活动接口
 *
 * @author paulG
 * @since 2020/11/17 2:30 下午
 */
@Tag(name = "买家端,秒杀活动接口")
@RestController
@RequestMapping("/buyer/promotion/seckill")
public class SeckillBuyerController {

    /**
     * 秒杀活动
     */
    @Autowired
    private SeckillApplyService seckillApplyService;
    @Autowired
    private BuyerStoreScopeSupport buyerStoreScopeSupport;

    @Operation(summary = "获取当天秒杀活动信息")
    @GetMapping
    public ResultMessage<List<SeckillTimelineVO>> getSeckillTime() {
        return ResultUtil.data(filterTimelineGoods(seckillApplyService.getSeckillTimeline()));
    }

    @Operation(summary = "获取某个时刻的秒杀活动商品信息")
    @GetMapping("/{timeline}")
    public ResultMessage<List<SeckillGoodsVO>> getSeckillGoods(@PathVariable Integer timeline) {
        return ResultUtil.data(filterGoods(seckillApplyService.getSeckillGoods(timeline)));
    }

    @Operation(summary = "分页获取某个时刻的秒杀活动商品信息")
    @GetMapping("/{timeline}/goods")
    public ResultMessage<IPage<SeckillGoodsVO>> getSeckillGoodsPage(@PathVariable Integer timeline,
                                                                    String goodsName,
                                                                    String categoryPath,
                                                                    PageVO pageVo) {
        return ResultUtil.data(seckillApplyService.getSeckillGoodsPage(
                timeline,
                goodsName,
                categoryPath,
                buyerStoreScopeSupport.listVisibleStoreIds(),
                pageVo
        ));
    }

    private List<SeckillTimelineVO> filterTimelineGoods(List<SeckillTimelineVO> timelines) {
        if (timelines == null || timelines.isEmpty()) {
            return Collections.emptyList();
        }
        for (SeckillTimelineVO timeline : timelines) {
            timeline.setSeckillGoodsList(filterGoods(timeline.getSeckillGoodsList()));
        }
        return timelines;
    }

    private List<SeckillGoodsVO> filterGoods(List<SeckillGoodsVO> goodsList) {
        List<String> visibleStoreIds = buyerStoreScopeSupport.listVisibleStoreIds();
        if (goodsList == null || goodsList.isEmpty()) {
            return Collections.emptyList();
        }
        if (visibleStoreIds.isEmpty()) {
            return goodsList;
        }
        return goodsList.stream()
                .filter(item -> item != null
                        && CharSequenceUtil.isNotBlank(item.getStoreId())
                        && visibleStoreIds.contains(item.getStoreId()))
                .collect(Collectors.toList());
    }

}
