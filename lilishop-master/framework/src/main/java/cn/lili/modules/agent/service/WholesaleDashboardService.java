package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.vos.WholesaleDashboardVO;

/**
 * 批发商城平台工作台服务
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface WholesaleDashboardService {

    /**
     * 查询批发商城平台工作台
     *
     * @return 工作台聚合视图
     */
    WholesaleDashboardVO dashboard();
}
