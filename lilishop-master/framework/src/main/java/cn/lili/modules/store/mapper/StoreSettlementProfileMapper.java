package cn.lili.modules.store.mapper;

import cn.hutool.core.date.DateTime;
import cn.lili.modules.store.entity.dos.StoreSettlementProfile;
import cn.lili.modules.store.entity.dto.StoreSettlementDay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 店铺结算资料数据层
 *
 * @author OpenAI
 * @since 2026/6/26
 */
public interface StoreSettlementProfileMapper extends BaseMapper<StoreSettlementProfile> {

    @Select("SELECT store_id, settlement_day FROM li_store_settlement_profile " +
            "WHERE settlement_cycle LIKE concat(#{day},',%') " +
            "OR settlement_cycle LIKE concat('%,',#{day},',%') " +
            "OR settlement_cycle LIKE concat('%,',#{day}) " +
            "OR settlement_cycle = #{day}")
    List<StoreSettlementDay> getSettlementStore(int day);

    @Update("UPDATE li_store_settlement_profile SET settlement_day=#{dateTime} WHERE store_id=#{storeId}")
    void updateSettlementDay(String storeId, DateTime dateTime);
}
