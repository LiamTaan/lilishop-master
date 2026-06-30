package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.StockReason;
import cn.lili.modules.procurement.service.StockReasonService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 管理端,出入库原因接口
 * 提供按条件分页查询原因
 *
 * @author Bulbasaur
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/manager/procurement/reason")
public class StockReasonManagerController {

    @Autowired
    private StockReasonService stockReasonService;

    @GetMapping
    public ResultMessage<IPage<StockReason>> page(@RequestParam(required = false) String reason,
                                                 @RequestParam(required = false) String category,
                                                 PageVO pageVO) {
        return ResultUtil.data(stockReasonService.page(reason, category, pageVO));
    }

    @PostMapping
    public ResultMessage<StockReason> add(@RequestBody StockReason stockReason) {
        stockReasonService.save(stockReason);
        return ResultUtil.data(stockReason);
    }

    @PutMapping
    public ResultMessage<Boolean> update(@RequestBody StockReason stockReason) {
        return ResultUtil.data(stockReasonService.updateById(stockReason));
    }

    @DeleteMapping("/{ids}")
    public ResultMessage<Boolean> delete(@PathVariable String ids) {
        return ResultUtil.data(stockReasonService.removeByIds(Arrays.asList(ids.split(","))));
    }
}
