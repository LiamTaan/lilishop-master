package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.entity.dto.OperationShortcutNavDTO;
import cn.lili.modules.page.service.OperationShortcutNavService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理端,首页分类配置接口")
@RequestMapping("/manager/other/shortcutNav")
public class OperationShortcutNavManagerController {

    @Autowired
    private OperationShortcutNavService operationShortcutNavService;

    @Operation(summary = "分页获取首页分类配置")
    @GetMapping
    public ResultMessage<IPage<OperationShortcutNav>> page(PageVO pageVO, OperationShortcutNavDTO query) {
        return ResultUtil.data(operationShortcutNavService.pageData(pageVO, query));
    }

    @Operation(summary = "获取首页分类配置详情")
    @Parameter(name = "id", description = "首页分类配置ID", required = true)
    @GetMapping("/{id}")
    public ResultMessage<OperationShortcutNav> get(@PathVariable String id) {
        return ResultUtil.data(operationShortcutNavService.getById(id));
    }

    @DemoSite
    @Operation(summary = "新增首页分类配置")
    @PostMapping
    public ResultMessage<OperationShortcutNav> add(@Valid OperationShortcutNav operationShortcutNav) {
        operationShortcutNavService.save(operationShortcutNav);
        return ResultUtil.data(operationShortcutNav);
    }

    @DemoSite
    @Operation(summary = "修改首页分类配置")
    @PutMapping("/{id}")
    public ResultMessage<OperationShortcutNav> edit(@PathVariable String id, @Valid OperationShortcutNav operationShortcutNav) {
        operationShortcutNav.setId(id);
        operationShortcutNavService.updateById(operationShortcutNav);
        return ResultUtil.data(operationShortcutNav);
    }

    @DemoSite
    @Operation(summary = "删除首页分类配置")
    @DeleteMapping("/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        operationShortcutNavService.removeById(id);
        return ResultUtil.success();
    }
}
