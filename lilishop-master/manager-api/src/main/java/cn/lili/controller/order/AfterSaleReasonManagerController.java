package cn.lili.controller.order;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.aftersale.entity.dos.AfterSaleReason;
import cn.lili.modules.order.aftersale.service.AfterSaleReasonService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,售后原因接口
 *
 * @author Bulbasaur
 * @since 2021/1/6 14:11
 */
@RestController
@RequestMapping("/manager/order/afterSaleReason")
public class AfterSaleReasonManagerController {

    /**
     * 售后原因
     */
    @Autowired
    private AfterSaleReasonService afterSaleReasonService;

    @GetMapping("/{id}")
    public ResultMessage<AfterSaleReason> get(@PathVariable String id) {

        return ResultUtil.data(afterSaleReasonService.getById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<AfterSaleReason>> getByPage(PageVO page, @RequestParam String serviceType) {
        return ResultUtil.data(afterSaleReasonService.pageByServiceType(page, serviceType));
    }

    @PostMapping
    public ResultMessage<AfterSaleReason> save(@Valid AfterSaleReason afterSaleReason) {
        afterSaleReasonService.save(afterSaleReason);
        return ResultUtil.data(afterSaleReason);
    }

    @PutMapping("update/{id}")
    public ResultMessage<AfterSaleReason> update(@Valid AfterSaleReason afterSaleReason, @PathVariable("id") String id) {
        afterSaleReason.setId(id);
        return ResultUtil.data(afterSaleReasonService.editAfterSaleReason(afterSaleReason));
    }

    @DeleteMapping("/delByIds/{id}")
    public ResultMessage<Object> delAllByIds(@PathVariable String id) {
        afterSaleReasonService.removeById(id);
        return ResultUtil.success();
    }
}
