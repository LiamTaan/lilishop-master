package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentDashboardOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationVO;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.agent.service.AgentDashboardService;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.store.entity.vos.StoreApplyVO;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.store.support.BuyerStoreScopeSupport;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 买家端代理商接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "买家端,代理商接口")
@RequestMapping("/buyer/agent")
public class AgentBuyerController {

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Autowired
    private StoreDetailService storeDetailService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private AgentReconciliationService agentReconciliationService;

    @Autowired
    private AgentDashboardService agentDashboardService;

    @Autowired
    private BuyerStoreScopeSupport buyerStoreScopeSupport;

    @Operation(summary = "查询当前用户是否代理商")
    @GetMapping("/role/check")
    public ResultMessage<Boolean> checkRole() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(authUser != null && agentRoleRelationService.isAgent(authUser.getId()));
    }

    @Operation(summary = "查询当前代理商绑定店铺")
    @GetMapping("/store")
    public ResultMessage<List<AgentStoreBindVO>> myStores() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(agentStoreBindService.listApprovedBindsByAgentMemberId(authUser.getId()));
    }

    @Operation(summary = "查询当前用户店铺申请")
    @GetMapping("/store/apply")
    public ResultMessage<StoreApplyVO> myStoreApply() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(storeDetailService.getStoreApplyVOByMemberId(authUser.getId()));
    }

    @Operation(summary = "查询当前代理商经营概览")
    @GetMapping("/dashboard/overview")
    public ResultMessage<AgentDashboardOverviewVO> overview() {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentDashboardService.overview(authUser.getId()));
    }

    @Operation(summary = "分页查询当前代理商可见批发商品")
    @GetMapping("/goods/wholesale")
    public ResultMessage<IPage<GoodsVO>> myWholesaleGoods(GoodsSearchParams goodsSearchParams, PageVO pageVO) {
        this.getCurrentAgent();
        buyerStoreScopeSupport.applyGoodsScope(goodsSearchParams, LoginIdentityCodeEnum.AGENT);
        goodsSearchParams.setSalesModel(GoodsSalesModeEnum.WHOLESALE.name());
        goodsSearchParams.setAuthFlag(GoodsAuthEnum.PASS.name());
        goodsSearchParams.setMarketEnable(GoodsStatusEnum.UPPER.name());
        IPage<cn.lili.modules.goods.entity.dos.Goods> page = goodsService.queryByParams(goodsSearchParams);
        List<GoodsVO> records = page.getRecords().stream().map(item -> goodsService.getGoodsVO(item.getId())).collect(Collectors.toList());
        return ResultUtil.data(PageUtil.convertPage(page, records));
    }

    @Operation(summary = "查询当前代理商可见批发商品详情")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/goods/wholesale/{goodsId}")
    public ResultMessage<GoodsVO> wholesaleGoodsDetail(@PathVariable String goodsId) {
        this.getCurrentAgent();
        GoodsVO goodsVO = goodsService.getGoodsVO(goodsId);
        if (!GoodsSalesModeEnum.WHOLESALE.name().equals(goodsVO.getSalesModel())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        return ResultUtil.data(goodsVO);
    }

    @Operation(summary = "分页查询代理商采购对账")
    @GetMapping("/procurement/reconciliation")
    public ResultMessage<IPage<AgentProcurementReconciliationVO>> procurementReconciliation(AgentProcurementReconciliationSearchParams params) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.procurementPage(authUser.getId(), params));
    }

    @Operation(summary = "查询代理商采购对账汇总")
    @GetMapping("/procurement/reconciliation/summary")
    public ResultMessage<AgentProcurementReconciliationSummaryVO> procurementReconciliationSummary(AgentProcurementReconciliationSearchParams params) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.procurementSummary(authUser.getId(), params));
    }

    @Operation(summary = "查询代理商采购对账详情")
    @Parameter(name = "id", description = "采购单ID", required = true)
    @GetMapping("/procurement/reconciliation/{id}")
    public ResultMessage<AgentProcurementReconciliationVO> procurementReconciliationDetail(@PathVariable String id) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.procurementDetail(authUser.getId(), id));
    }

    @Operation(summary = "分页查询代理商资金对账")
    @GetMapping("/fund/reconciliation")
    public ResultMessage<IPage<AgentFundReconciliationVO>> fundReconciliation(AgentFundReconciliationSearchParams params) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.fundPage(authUser.getId(), params));
    }

    @Operation(summary = "查询代理商资金对账汇总")
    @GetMapping("/fund/reconciliation/summary")
    public ResultMessage<AgentFundReconciliationSummaryVO> fundReconciliationSummary(AgentFundReconciliationSearchParams params) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.fundSummary(authUser.getId(), params));
    }

    @Operation(summary = "查询代理商资金对账详情")
    @Parameter(name = "id", description = "资金流水ID", required = true)
    @GetMapping("/fund/reconciliation/{id}")
    public ResultMessage<AgentFundReconciliationVO> fundReconciliationDetail(@PathVariable String id) {
        AuthUser authUser = this.getCurrentAgent();
        return ResultUtil.data(agentReconciliationService.fundDetail(authUser.getId(), id));
    }

    private AuthUser getCurrentAgent() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        if (!agentRoleRelationService.isAgent(authUser.getId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        return authUser;
    }
}
