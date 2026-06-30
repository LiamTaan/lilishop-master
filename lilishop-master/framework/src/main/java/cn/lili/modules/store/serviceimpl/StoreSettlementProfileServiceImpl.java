package cn.lili.modules.store.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.utils.SnowFlake;
import cn.lili.modules.store.entity.dos.StoreSettlementProfile;
import cn.lili.modules.store.entity.dto.StoreSettlementDay;
import cn.lili.modules.store.entity.dto.StoreSettlementProfileDTO;
import cn.lili.modules.store.mapper.StoreSettlementProfileMapper;
import cn.lili.modules.store.service.StoreSettlementProfileService;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 店铺结算资料业务层实现
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Service
public class StoreSettlementProfileServiceImpl extends ServiceImpl<StoreSettlementProfileMapper, StoreSettlementProfile>
        implements StoreSettlementProfileService {

    @Autowired
    private StoreService storeService;

    @Override
    public StoreSettlementProfile getByStoreId(String storeId) {
        return this.getOne(new LambdaQueryWrapper<StoreSettlementProfile>()
                .eq(StoreSettlementProfile::getStoreId, storeId)
                .last("limit 1"), false);
    }

    @Override
    public StoreSettlementProfileDTO getProfile(String storeId) {
        StoreSettlementProfile profile = this.getByStoreId(storeId);
        if (profile == null) {
            return null;
        }
        StoreSettlementProfileDTO dto = new StoreSettlementProfileDTO();
        BeanUtil.copyProperties(profile, dto);
        return dto;
    }

    @Override
    public boolean saveOrUpdateProfile(String storeId, StoreSettlementProfileDTO profileDTO) {
        if (storeService.getById(storeId) == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        StoreSettlementProfile profile = this.getByStoreId(storeId);
        if (profile == null) {
            profile = new StoreSettlementProfile();
            profile.setId(SnowFlake.createStr("SSP"));
            profile.setStoreId(storeId);
            profile.setSettlementDay(new Date());
        }
        BeanUtil.copyProperties(profileDTO, profile);
        profile.setStoreId(storeId);
        if (profile.getSettlementDay() == null) {
            profile.setSettlementDay(new Date());
        }
        return this.saveOrUpdate(profile);
    }

    @Override
    public List<StoreSettlementDay> getSettlementStore(int day) {
        return this.baseMapper.getSettlementStore(day);
    }

    @Override
    public void updateSettlementDay(String storeId, DateTime dateTime) {
        this.baseMapper.updateSettlementDay(storeId, dateTime);
    }
}
