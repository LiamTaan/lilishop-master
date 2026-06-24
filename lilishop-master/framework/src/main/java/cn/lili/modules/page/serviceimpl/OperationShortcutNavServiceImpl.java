package cn.lili.modules.page.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.SwitchEnum;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.entity.dto.OperationShortcutNavDTO;
import cn.lili.modules.page.mapper.OperationShortcutNavMapper;
import cn.lili.modules.page.service.OperationShortcutNavService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页分类配置业务层实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class OperationShortcutNavServiceImpl extends ServiceImpl<OperationShortcutNavMapper, OperationShortcutNav> implements OperationShortcutNavService {

    @Override
    public IPage<OperationShortcutNav> pageData(PageVO pageVO, OperationShortcutNavDTO query) {
        LambdaQueryWrapper<OperationShortcutNav> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotBlank(query.getClientType()), OperationShortcutNav::getClientType, query.getClientType());
        queryWrapper.eq(CharSequenceUtil.isNotBlank(query.getDisplayStatus()), OperationShortcutNav::getDisplayStatus, query.getDisplayStatus());
        queryWrapper.like(CharSequenceUtil.isNotBlank(query.getTitle()), OperationShortcutNav::getTitle, query.getTitle());
        queryWrapper.orderByAsc(OperationShortcutNav::getSortOrder).orderByDesc(OperationShortcutNav::getCreateTime);
        return this.page(PageUtil.initPage(pageVO), queryWrapper);
    }

    @Override
    public List<OperationShortcutNav> listBuyerNav(String clientType) {
        LambdaQueryWrapper<OperationShortcutNav> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotBlank(clientType), OperationShortcutNav::getClientType, clientType);
        queryWrapper.eq(OperationShortcutNav::getDisplayStatus, SwitchEnum.OPEN.name());
        queryWrapper.orderByAsc(OperationShortcutNav::getSortOrder).orderByDesc(OperationShortcutNav::getCreateTime);
        return this.list(queryWrapper);
    }
}
