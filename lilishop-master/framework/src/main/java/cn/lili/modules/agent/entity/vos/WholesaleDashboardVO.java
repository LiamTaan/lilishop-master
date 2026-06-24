package cn.lili.modules.agent.entity.vos;

import cn.lili.modules.store.entity.vos.StoreAuditSummaryVO;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.IndexStatisticsVO;
import cn.lili.modules.statistics.entity.vo.CategoryStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.StoreStatisticsDataVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordSummaryVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批发商城平台工作台视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class WholesaleDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "首页基础概览")
    private IndexStatisticsVO indexStatistics;

    @Schema(description = "热卖店铺排行")
    private List<StoreStatisticsDataVO> storeRankList;

    @Schema(description = "热卖商品排行")
    private List<GoodsStatisticsDataVO> goodsRankList;

    @Schema(description = "类目销售排行")
    private List<CategoryStatisticsDataVO> categoryRankList;

    @Schema(description = "平台分账余额概览")
    private ProfitSharingBalanceVO profitSharingBalance;

    @Schema(description = "代理商资金对账汇总")
    private AgentFundReconciliationSummaryVO agentFundSummary;

    @Schema(description = "代理商采购对账汇总")
    private AgentProcurementReconciliationSummaryVO procurementSummary;

    @Schema(description = "店铺治理汇总")
    private StoreAuditSummaryVO storeAuditSummary;

    @Schema(description = "批发平台经营趋势")
    private WholesaleBusinessTrendVO businessTrend;

    @Schema(description = "核销治理汇总")
    private VerificationRecordSummaryVO verificationSummary;

    @Schema(description = "平台治理总览")
    private WholesaleGovernanceSummaryVO governanceSummary;

    @Schema(description = "代理商经营概览")
    private AgentOverviewVO agentOverview;
}
