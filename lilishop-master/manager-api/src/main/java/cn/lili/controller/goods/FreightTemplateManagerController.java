package cn.lili.controller.goods;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.store.entity.vos.FreightTemplateVO;
import cn.lili.modules.store.service.FreightTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,商品发品辅助接口
 *
 * @author Codex
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/manager/goods/freightTemplate")
public class FreightTemplateManagerController {

    @Autowired
    private FreightTemplateService freightTemplateService;

    @GetMapping("/store/{storeId}")
    public ResultMessage<List<FreightTemplateVO>> getStoreTemplates(@PathVariable String storeId) {
        return ResultUtil.data(freightTemplateService.getFreightTemplateList(storeId));
    }
}
