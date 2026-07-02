package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.promotion.entity.dos.Seckill;
import cn.lili.modules.promotion.entity.dos.SeckillApply;
import cn.lili.modules.promotion.entity.dto.SeckillApplyManagerDTO;
import cn.lili.modules.promotion.entity.dto.search.SeckillSearchParams;
import cn.lili.modules.promotion.entity.vos.SeckillVO;
import cn.lili.modules.promotion.service.SeckillApplyService;
import cn.lili.modules.promotion.service.SeckillService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 管理端,秒杀活动接口
 *
 * @author paulG
 * @since 2020/8/20
 **/
@RestController
@Validated
@RequestMapping("/manager/promotion/seckill")
public class SeckillManagerController {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private SeckillApplyService seckillApplyService;


    @GetMapping("/init")
    public void addSeckill() {
        seckillService.init();
    }


    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResultMessage<Seckill> updateSeckill(@RequestBody SeckillVO seckillVO) {
        seckillService.updatePromotions(seckillVO);
        return ResultUtil.data(seckillVO);
    }

    @GetMapping("/{id}")
    public ResultMessage<Seckill> get(@PathVariable String id) {
        Seckill seckill = seckillService.getById(id);
        return ResultUtil.data(seckill);
    }

    @GetMapping
    public ResultMessage<IPage<Seckill>> getAll(SeckillSearchParams param, PageVO pageVo) {
        return ResultUtil.data(seckillService.pageFindAll(param, pageVo));
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Object> deleteSeckill(@PathVariable String id) {
        seckillService.removePromotions(Collections.singletonList(id));
        return ResultUtil.success();
    }

    @PutMapping("/status/{id}")
    public ResultMessage<Object> updateSeckillStatus(@PathVariable String id, Long startTime, Long endTime) {
        seckillService.updateStatus(Collections.singletonList(id), startTime, endTime);
        return ResultUtil.success();
    }

    @GetMapping("/apply")
    public ResultMessage<IPage<SeckillApply>> getSeckillApply(SeckillSearchParams param, PageVO pageVo) {
        IPage<SeckillApply> seckillApply = seckillApplyService.getSeckillApplyPage(param, pageVo);
        return ResultUtil.data(seckillApply);
    }

    @GetMapping("/apply/{seckillId}/available-sku")
    public ResultMessage<IPage<GoodsSku>> getAvailableSku(@PathVariable String seckillId, Integer timeLine, GoodsSkuSearchParams param) {
        return ResultUtil.data(seckillApplyService.getAvailableSkuPage(seckillId, param, timeLine));
    }

    @PostMapping(path = "/apply/{seckillId}", consumes = "application/json", produces = "application/json")
    public ResultMessage<String> addSeckillApply(@PathVariable String seckillId, @RequestBody List<@Valid SeckillApplyManagerDTO> applyList) {
        seckillApplyService.addSeckillApplyByManager(seckillId, applyList);
        return ResultUtil.success();
    }

    @PutMapping(path = "/apply/{seckillId}/{id}", consumes = "application/json", produces = "application/json")
    public ResultMessage<String> updateSeckillApply(@PathVariable String seckillId, @PathVariable String id, @RequestBody @Valid SeckillApplyManagerDTO apply) {
        seckillApplyService.updateSeckillApplyByManager(seckillId, id, apply);
        return ResultUtil.success();
    }

    @DeleteMapping("/apply/{seckillId}/{id}")
    public ResultMessage<String> deleteSeckillApply(@PathVariable String seckillId, @PathVariable String id) {
        seckillApplyService.removeSeckillApply(seckillId, id);
        return ResultUtil.success();
    }


}
