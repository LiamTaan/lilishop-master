package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;

import java.util.List;

/**
 * 供货端数据概览统计服务
 *
 * @author dawn
 * @since 2026/7/2
 */
public interface StoreDashboardAnalyticsService {

    /**
     * 查询当前店铺工作台首页
     *
     * @param storeId 店铺ID
     * @return 工作台首页
     */
    StoreWorkbenchOverviewVO overview(String storeId);

    /**
     * 查询当前店铺趋势图
     *
     * @param storeId 店铺ID
     * @param params 查询参数
     * @return 趋势图
     */
    StoreDashboardTrendVO trend(String storeId, StoreDashboardTrendQueryParams params);

    /**
     * 查询当前店铺趋势明细
     *
     * @param storeId 店铺ID
     * @param params 查询参数
     * @return 趋势明细
     */
    List<StoreDashboardTrendDetailVO> trendDetail(String storeId, StoreDashboardTrendQueryParams params);
}
