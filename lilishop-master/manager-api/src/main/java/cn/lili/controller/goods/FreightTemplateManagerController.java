package cn.lili.controller.goods;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.store.entity.vos.FreightTemplateVO;
import cn.lili.modules.store.service.FreightTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理端,运费模板接口
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

    @GetMapping("/{id}")
    public ResultMessage<FreightTemplateVO> getTemplate(@PathVariable String id) {
        return ResultUtil.data(freightTemplateService.getFreightTemplate(id));
    }

    @PostMapping
    public ResultMessage<FreightTemplateVO> save(@Valid @RequestBody FreightTemplateVO freightTemplateVO) {
        return ResultUtil.data(freightTemplateService.addFreightTemplate(freightTemplateVO.getStoreId(), freightTemplateVO));
    }

    @PutMapping("/{id}")
    public ResultMessage<FreightTemplateVO> update(@PathVariable String id, @Valid @RequestBody FreightTemplateVO freightTemplateVO) {
        return ResultUtil.data(freightTemplateService.editFreightTemplate(freightTemplateVO.getStoreId(), id, freightTemplateVO));
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Object> delete(@PathVariable String id, @RequestParam String storeId) {
        freightTemplateService.removeFreightTemplate(storeId, id);
        return ResultUtil.success();
    }
}
