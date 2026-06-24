package cn.lili.modules.store.serviceimpl;

import cn.lili.modules.store.entity.dos.StoreAuditLog;
import cn.lili.modules.store.entity.vos.StoreAuditLogVO;
import cn.lili.modules.store.mapper.StoreAuditLogMapper;
import cn.lili.modules.store.service.StoreAuditLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 店铺审核历史业务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class StoreAuditLogServiceImpl extends ServiceImpl<StoreAuditLogMapper, StoreAuditLog> implements StoreAuditLogService {

    @Override
    public List<StoreAuditLogVO> listByStoreId(String storeId) {
        return this.baseMapper.listByStoreId(storeId);
    }
}
