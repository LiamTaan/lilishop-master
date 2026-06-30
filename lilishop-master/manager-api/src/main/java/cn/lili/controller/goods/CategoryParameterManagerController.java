package cn.lili.controller.goods;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Parameters;
import cn.lili.modules.goods.service.ParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,分类绑定参数接口
 *
 * @author Codex
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/manager/goods/categoryParameters")
public class CategoryParameterManagerController {

    @Autowired
    private ParametersService parametersService;

    @GetMapping("/{categoryId}")
    public ResultMessage<List<Parameters>> getCategoryParam(@PathVariable String categoryId) {
        return ResultUtil.data(parametersService.getParametersByCategoryId(categoryId));
    }
}
