package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.Special;
import cn.lili.modules.page.service.SpecialService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理端,专题活动接口
 *
 * @author Bulbasaur
 * @since 2020/12/7 11:33
 */
@RestController
@RequestMapping("/manager/other/special")
public class SpecialManagerController {

    @Autowired
    private SpecialService specialService;

    @GetMapping
    public ResultMessage<IPage<Special>> getByPage(PageVO page) {
        return ResultUtil.data(specialService.page(PageUtil.initPage(page)));
    }

    @GetMapping("/{id}")
    public ResultMessage<Special> get(
            @PathVariable String id) {
        return ResultUtil.data(specialService.getById(id));
    }

    @PostMapping
    public ResultMessage<Special> add(@Valid @RequestBody Special special) {
        specialService.save(special);
        return ResultUtil.data(special);
    }

    @PutMapping("/{id}")
    public ResultMessage<Special> edit(
            @PathVariable String id, @Valid @RequestBody Special special) {
        special.setId(id);
        specialService.updateById(special);
        return ResultUtil.data(special);
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Object> delAllByIds(
            @PathVariable String id) {
        specialService.removeById(id);
        return ResultUtil.success();
    }

}
