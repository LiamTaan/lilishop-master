package cn.lili.controller.statistics;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardVO;
import cn.lili.modules.agent.service.StoreDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 店铺端经营概览聚合接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "店铺端,经营概览聚合接口")
@RequestMapping("/store/dashboard")
public class StoreDashboardController {

    @Autowired
    private StoreDashboardService storeDashboardService;

    @Operation(summary = "查询当前店铺经营概览")
    @GetMapping("/overview")
    public ResultMessage<StoreDashboardVO> dashboard() {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.dashboard(storeId));
    }

    @Operation(summary = "查询当前店铺资产概览")
    @GetMapping("/assets")
    public ResultMessage<StoreAssetOverviewVO> assets() {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.assets(storeId));
    }
}
