package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.system.entity.dos.Logistics;
import cn.lili.modules.system.service.LogisticsService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理端,物流公司接口
 *
 * @author Chopper
 * @since 2020/11/17 7:56 下午
 */
@RestController
@RequestMapping("/manager/other/logistics")
public class LogisticsManagerController {
    @Autowired
    private LogisticsService logisticsService;

    @GetMapping("/{id}")
    public ResultMessage<Logistics> get(
 @PathVariable String id) {
        return ResultUtil.data(logisticsService.getById(id));
    }

    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<Logistics>> getByPage(PageVO page) {
        return ResultUtil.data(logisticsService.page(PageUtil.initPage(page)));
    }

    @PutMapping("/{id}")
    public ResultMessage<Logistics> update(
 @NotNull @PathVariable String id, @Valid Logistics logistics) {
        logistics.setId(id);
        logisticsService.updateById(logistics);
        return ResultUtil.data(logistics);
    }

    @PostMapping
    public ResultMessage<Logistics> save(@Valid Logistics logistics) {
        logisticsService.save(logistics);
        return ResultUtil.data(logistics);
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Object> delAllByIds(
 @PathVariable String id) {
        logisticsService.removeById(id);
        return ResultUtil.success();
    }
}
