package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.params.PlatformFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.PlatformProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.PlatformFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.PlatformProcurementReconciliationVO;
import cn.lili.modules.agent.service.AgentReconciliationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端平台对账接口
 *
 * @author dawn
 * @since 2026/6/19
 */
@RestController
@Tag(name = "管理端,平台对账接口")
@RequestMapping("/manager/reconciliation")
public class PlatformReconciliationManagerController {

    @Autowired
    private AgentReconciliationService agentReconciliationService;

    @Operation(summary = "分页查询平台采购对账")
    @GetMapping("/purchase")
    public ResultMessage<IPage<PlatformProcurementReconciliationVO>> procurementPage(PlatformProcurementReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.platformProcurementPage(searchParams));
    }

    @Operation(summary = "分页查询平台资金对账")
    @GetMapping("/fund")
    public ResultMessage<IPage<PlatformFundReconciliationVO>> fundPage(PlatformFundReconciliationSearchParams searchParams) {
        return ResultUtil.data(agentReconciliationService.platformFundPage(searchParams));
    }
}
