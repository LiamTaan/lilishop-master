package cn.lili.modules.page.service;

import cn.lili.modules.page.entity.vos.PlatformHomeVO;

/**
 * 平台首页聚合服务
 *
 * @author codex
 * @since 2026/6/25
 */
public interface PlatformHomeService {

    /**
     * 获取结构化平台首页
     *
     * @param clientType 客户端类型
     * @return 平台首页
     */
    PlatformHomeVO getPlatformHome(String clientType);
}
