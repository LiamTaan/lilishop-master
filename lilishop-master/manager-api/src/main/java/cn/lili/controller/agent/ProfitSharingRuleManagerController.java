package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.dos.ProfitSharingRule;
import cn.lili.modules.agent.entity.dto.ProfitSharingRuleDTO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRulePageVO;
import cn.lili.modules.agent.service.ProfitSharingRuleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 平台分账规则管理接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "管理端,平台分账规则接口")
@RequestMapping("/manager/profitsharing/rule")
public class ProfitSharingRuleManagerController {

    @Autowired
    private ProfitSharingRuleService profitSharingRuleService;

    @Operation(summary = "分页查询规则")
    @GetMapping
    public ResultMessage<IPage<ProfitSharingRulePageVO>> page(PageVO page) {
        return ResultUtil.data(profitSharingRuleService.page(page));
    }

    @Operation(summary = "新增规则")
    @PostMapping
    public ResultMessage<ProfitSharingRule> create(@Valid @RequestBody ProfitSharingRuleDTO dto) {
        return ResultUtil.data(profitSharingRuleService.create(dto));
    }

    @Operation(summary = "修改规则")
    @Parameter(name = "id", description = "规则ID", required = true)
    @PutMapping("/{id}")
    public ResultMessage<ProfitSharingRule> update(@PathVariable String id, @Valid @RequestBody ProfitSharingRuleDTO dto) {
        return ResultUtil.data(profitSharingRuleService.update(id, dto));
    }

    @Operation(summary = "删除规则")
    @Parameter(name = "id", description = "规则ID", required = true)
    @DeleteMapping("/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        profitSharingRuleService.delete(id);
        return ResultUtil.success();
    }
}
