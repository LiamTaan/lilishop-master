package cn.lili.modules.agent.entity.vos;

import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.OrderOverviewVO;
import cn.lili.modules.statistics.entity.vo.StoreIndexStatisticsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 供货端经营概览视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class StoreDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺首页概览")
    private StoreIndexStatisticsVO storeIndexStatistics;

    @Schema(description = "订单概览")
    private OrderOverviewVO orderOverview;

    @Schema(description = "热卖商品排行")
    private List<GoodsStatisticsDataVO> goodsRankList;
}
