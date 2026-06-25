package cn.lili.modules.page.service;

import cn.lili.modules.page.entity.dto.PlatformHomeConfigSaveDTO;
import cn.lili.modules.page.entity.vos.PlatformHomeConfigVO;

/**
 * 首页配置聚合服务
 *
 * @author codex
 * @since 2026/6/25
 */
public interface PlatformHomeConfigService {

    /**
     * 获取首页配置
     *
     * @param clientType 客户端类型
     * @return 首页配置
     */
    PlatformHomeConfigVO getConfig(String clientType);

    /**
     * 保存首页配置
     *
     * @param saveDTO 保存 DTO
     * @return 最新首页配置
     */
    PlatformHomeConfigVO saveConfig(PlatformHomeConfigSaveDTO saveDTO);
}
