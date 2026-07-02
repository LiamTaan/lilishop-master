package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;

import java.util.List;

/**
 * 供货端经营概览服务
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface StoreDashboardService {

    /**
     * 查询当前店铺工作台首页
     *
     * @param storeId 店铺ID
     * @return 工作台首页
     */
    StoreWorkbenchOverviewVO dashboard(String storeId);

    /**
     * 查询当前店铺经营趋势
     *
     * @param storeId 店铺ID
     * @param params 查询参数
     * @return 趋势图
     */
    StoreDashboardTrendVO trend(String storeId, StoreDashboardTrendQueryParams params);

    /**
     * 查询当前店铺经营趋势明细
     *
     * @param storeId 店铺ID
     * @param params 查询参数
     * @return 趋势明细
     */
    List<StoreDashboardTrendDetailVO> trendDetail(String storeId, StoreDashboardTrendQueryParams params);

    /**
     * 查询当前店铺资产概览
     *
     * @param storeId 店铺ID
     * @return 资产概览
     */
    StoreAssetOverviewVO assets(String storeId);
}
