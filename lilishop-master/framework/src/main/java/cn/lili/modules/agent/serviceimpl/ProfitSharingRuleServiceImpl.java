package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.BeanUtil;
import cn.lili.modules.agent.entity.dos.ProfitSharingRule;
import cn.lili.modules.agent.entity.dto.ProfitSharingRuleDTO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRulePageVO;
import cn.lili.modules.agent.mapper.ProfitSharingRuleMapper;
import cn.lili.modules.agent.service.ProfitSharingRuleService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台分账规则业务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class ProfitSharingRuleServiceImpl extends ServiceImpl<ProfitSharingRuleMapper, ProfitSharingRule> implements ProfitSharingRuleService {

    @Override
    public IPage<ProfitSharingRulePageVO> page(cn.lili.common.vo.PageVO page) {
        LambdaQueryWrapper<ProfitSharingRule> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(ProfitSharingRule::getCreateTime);
        IPage<ProfitSharingRule> pageResult = this.page(PageUtil.initPage(page), wrapper);
        IPage<ProfitSharingRulePageVO> result = PageUtil.convertPage(pageResult, pageResult.getRecords().stream().map(item -> {
            ProfitSharingRulePageVO vo = new ProfitSharingRulePageVO();
            BeanUtil.copyProperties(item, vo);
            return vo;
        }).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfitSharingRule create(ProfitSharingRuleDTO dto) {
        ProfitSharingRule rule = new ProfitSharingRule();
        BeanUtil.copyProperties(dto, rule);
        this.save(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfitSharingRule update(String id, ProfitSharingRuleDTO dto) {
        ProfitSharingRule rule = this.getById(id);
        if (rule == null) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        BeanUtil.copyProperties(dto, rule);
        this.updateById(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        ProfitSharingRule rule = this.getById(id);
        if (rule == null) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        this.removeById(id);
    }
}
