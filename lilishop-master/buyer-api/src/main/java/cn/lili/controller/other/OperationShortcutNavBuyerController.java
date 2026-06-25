package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.service.OperationShortcutNavService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端,首页分类快捷入口接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "买家端,首页分类快捷入口接口")
@RequestMapping("/buyer/other/shortcutNav")
public class OperationShortcutNavBuyerController {

    @Autowired
    private OperationShortcutNavService operationShortcutNavService;

    @Operation(summary = "获取首页分类快捷入口")
    @GetMapping
    public ResultMessage<List<OperationShortcutNav>> list(@RequestParam(required = false) String clientType) {
        return ResultUtil.data(operationShortcutNavService.listBuyerNav(clientType));
    }
}
