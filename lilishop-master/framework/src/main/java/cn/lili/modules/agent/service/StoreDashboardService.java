package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardVO;

/**
 * 供货端经营概览服务
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface StoreDashboardService {

    /**
     * 查询当前店铺经营概览
     *
     * @param storeId 店铺ID
     * @return 概览
     */
    StoreDashboardVO dashboard(String storeId);

    /**
     * 查询当前店铺资产概览
     *
     * @param storeId 店铺ID
     * @return 资产概览
     */
    StoreAssetOverviewVO assets(String storeId);
}
