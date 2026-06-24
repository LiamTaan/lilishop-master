package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.vos.WholesaleDashboardVO;
import cn.lili.modules.agent.service.WholesaleDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批发商城平台工作台接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "管理端,批发商城平台工作台接口")
@RequestMapping("/manager/dashboard/wholesale")
public class WholesaleDashboardManagerController {

    @Autowired
    private WholesaleDashboardService wholesaleDashboardService;

    @Operation(summary = "查询批发商城平台工作台")
    @GetMapping
    public ResultMessage<WholesaleDashboardVO> dashboard() {
        return ResultUtil.data(wholesaleDashboardService.dashboard());
    }
}
