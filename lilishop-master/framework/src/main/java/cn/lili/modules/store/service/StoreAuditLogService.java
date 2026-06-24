package cn.lili.modules.store.service;

import cn.lili.modules.store.entity.dos.StoreAuditLog;
import cn.lili.modules.store.entity.vos.StoreAuditLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 店铺审核历史业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface StoreAuditLogService extends IService<StoreAuditLog> {

    /**
     * 查询店铺审核历史
     *
     * @param storeId 店铺ID
     * @return 审核历史
     */
    List<StoreAuditLogVO> listByStoreId(String storeId);
}
