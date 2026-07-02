package cn.lili.controller.statistics;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;
import cn.lili.modules.agent.service.StoreDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 供货商端工作台接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "供货商端,工作台接口")
@RequestMapping("/store/dashboard")
public class StoreDashboardController {

    @Autowired
    private StoreDashboardService storeDashboardService;

    @Operation(summary = "供货商查询工作台首页")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = StoreWorkbenchOverviewVO.class)))
    })
    @GetMapping("/overview")
    public ResultMessage<StoreWorkbenchOverviewVO> dashboard() {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.dashboard(storeId));
    }

    @Operation(summary = "供货商查询数据概览趋势图")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = StoreDashboardTrendVO.class)))
    })
    @GetMapping("/trend")
    public ResultMessage<StoreDashboardTrendVO> trend(StoreDashboardTrendQueryParams params) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.trend(storeId, params));
    }

    @Operation(summary = "供货商查询数据概览趋势明细")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = StoreDashboardTrendDetailVO.class)))
    })
    @GetMapping("/trend/detail")
    public ResultMessage<List<StoreDashboardTrendDetailVO>> trendDetail(StoreDashboardTrendQueryParams params) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.trendDetail(storeId, params));
    }

    @Operation(summary = "供货商查询资产概览")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = StoreAssetOverviewVO.class)))
    })
    @GetMapping("/assets")
    public ResultMessage<StoreAssetOverviewVO> assets() {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeDashboardService.assets(storeId));
    }
}
