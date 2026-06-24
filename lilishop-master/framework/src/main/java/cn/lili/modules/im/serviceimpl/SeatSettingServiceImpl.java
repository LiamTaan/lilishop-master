package cn.lili.modules.im.serviceimpl;

import cn.hutool.core.util.StrUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.im.entity.dos.SeatSetting;
import cn.lili.modules.im.mapper.SeatSettingMapper;
import cn.lili.modules.im.service.SeatSettingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 坐席设置业务层实现
 *
 * @author pikachu
 * @since 2020-02-18 16:18:56
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SeatSettingServiceImpl extends ServiceImpl<SeatSettingMapper, SeatSetting> implements SeatSettingService {
    @Override
    public SeatSetting getSetting(String storeId) {
        LambdaQueryWrapper<SeatSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeatSetting::getTenantId, storeId);
        SeatSetting seatSetting = this.baseMapper.selectOne(queryWrapper);
        if (seatSetting == null) {
            seatSetting = new SeatSetting();
            seatSetting.setOutLineAutoReply("您好，我现在不在线，请您留下关键内容和联系方式，我看到后会立马回电。");
            seatSetting.setLongTermAutoReply("您好，我正在查阅相关资料，请您稍等。");
            seatSetting.setWelcome("您好，请问有什么可以帮您？");
            seatSetting.setTenantId(storeId);
            this.save(seatSetting);
        }
        return seatSetting;
    }

    @Override
    public SeatSetting updateByStore(SeatSetting seatSetting) {
        SeatSetting oldSetting = null;
        if (StrUtil.isNotBlank(seatSetting.getId())) {
            oldSetting = this.baseMapper.selectById(seatSetting.getId());
        }
        if (oldSetting == null && StrUtil.isNotBlank(seatSetting.getTenantId())) {
            LambdaQueryWrapper<SeatSetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SeatSetting::getTenantId, seatSetting.getTenantId());
            oldSetting = this.baseMapper.selectOne(queryWrapper);
        }
        if (oldSetting == null) {
            seatSetting.setId(null);
            this.save(seatSetting);
            return seatSetting;
        }
        if (!oldSetting.getTenantId().equals(seatSetting.getTenantId())) {
            throw new ServiceException(ResultCode.ERROR);
        }
        oldSetting.setWelcome(seatSetting.getWelcome());
        oldSetting.setOutLineAutoReply(seatSetting.getOutLineAutoReply());
        oldSetting.setLongTermAutoReply(seatSetting.getLongTermAutoReply());
        this.updateById(oldSetting);
        return oldSetting;
    }
}
