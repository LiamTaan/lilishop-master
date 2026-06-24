package cn.lili.modules.store.mapper;

import cn.lili.modules.store.entity.dos.StoreAuditLog;
import cn.lili.modules.store.entity.vos.StoreAuditLogVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 店铺审核历史数据层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface StoreAuditLogMapper extends BaseMapper<StoreAuditLog> {

    /**
     * 查询店铺审核历史
     *
     * @param storeId 店铺ID
     * @return 审核历史
     */
    @Select("SELECT id,store_id,from_audit_status,to_audit_status,audit_remark,operator_id,operator_name,create_time " +
            "FROM li_store_audit_log WHERE store_id = #{storeId} AND delete_flag = 0 ORDER BY create_time DESC")
    List<StoreAuditLogVO> listByStoreId(String storeId);
}
