package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.system.entity.dos.SensitiveWords;
import cn.lili.modules.system.service.SensitiveWordsService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理端,敏感词管理接口
 *
 * @author Bulbasaur
 * @since 2020-05-06 15:18:56
 */
@RestController
@RequestMapping("/manager/other/sensitiveWords")
public class SensitiveWordsManagerController {

    @Autowired
    private SensitiveWordsService sensitiveWordsService;

    @GetMapping("/{id}")
    public ResultMessage<SensitiveWords> get(
 @PathVariable String id) {
        return ResultUtil.data(sensitiveWordsService.getById(id));
    }

    @GetMapping
    public ResultMessage<IPage<SensitiveWords>> getByPage(PageVO page) {
        return ResultUtil.data(sensitiveWordsService.page(PageUtil.initPage(page)));
    }

    @PostMapping
    public ResultMessage<SensitiveWords> add(@Valid @RequestBody SensitiveWords sensitiveWords) {
        sensitiveWordsService.save(sensitiveWords);
        sensitiveWordsService.resetCache();
        return ResultUtil.data(sensitiveWords);
    }

    @PutMapping("/{id}")
    public ResultMessage<SensitiveWords> edit(
 @PathVariable String id, @RequestBody SensitiveWords sensitiveWords) {
        sensitiveWords.setId(id);
        sensitiveWordsService.updateById(sensitiveWords);
        sensitiveWordsService.resetCache();
        return ResultUtil.data(sensitiveWords);
    }

    @DeleteMapping("/delByIds/{ids}")
    public ResultMessage<Object> delAllByIds(
 @PathVariable List<String> ids) {
        sensitiveWordsService.removeByIds(ids);
        sensitiveWordsService.resetCache();
        return ResultUtil.success();
    }
}
