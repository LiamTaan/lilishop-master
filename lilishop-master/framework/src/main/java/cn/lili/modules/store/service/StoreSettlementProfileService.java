package cn.lili.modules.store.service;

import cn.hutool.core.date.DateTime;
import cn.lili.modules.store.entity.dos.StoreSettlementProfile;
import cn.lili.modules.store.entity.dto.StoreSettlementDay;
import cn.lili.modules.store.entity.dto.StoreSettlementProfileDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 店铺结算资料业务层
 *
 * @author OpenAI
 * @since 2026/6/26
 */
public interface StoreSettlementProfileService extends IService<StoreSettlementProfile> {

    StoreSettlementProfile getByStoreId(String storeId);

    StoreSettlementProfileDTO getProfile(String storeId);

    boolean saveOrUpdateProfile(String storeId, StoreSettlementProfileDTO profileDTO);

    List<StoreSettlementDay> getSettlementStore(int day);

    void updateSettlementDay(String storeId, DateTime dateTime);
}
