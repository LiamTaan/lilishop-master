package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.entity.dto.OperationShortcutNavDTO;
import cn.lili.modules.page.service.OperationShortcutNavService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端,首页分类配置接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@RequestMapping("/manager/other/shortcutNav")
public class OperationShortcutNavManagerController {

    @Autowired
    private OperationShortcutNavService operationShortcutNavService;

    @GetMapping
    public ResultMessage<IPage<OperationShortcutNav>> page(PageVO pageVO, OperationShortcutNavDTO query) {
        return ResultUtil.data(operationShortcutNavService.pageData(pageVO, query));
    }

    @GetMapping("/{id}")
    public ResultMessage<OperationShortcutNav> get(@PathVariable String id) {
        return ResultUtil.data(operationShortcutNavService.getById(id));
    }

    @DemoSite
    @PostMapping
    public ResultMessage<OperationShortcutNav> add(@Valid OperationShortcutNav operationShortcutNav) {
        operationShortcutNavService.save(operationShortcutNav);
        return ResultUtil.data(operationShortcutNav);
    }

    @DemoSite
    @PutMapping("/{id}")
    public ResultMessage<OperationShortcutNav> edit(@PathVariable String id, @Valid OperationShortcutNav operationShortcutNav) {
        operationShortcutNav.setId(id);
        operationShortcutNavService.updateById(operationShortcutNav);
        return ResultUtil.data(operationShortcutNav);
    }

    @DemoSite
    @DeleteMapping("/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        operationShortcutNavService.removeById(id);
        return ResultUtil.success();
    }
}
