package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.dto.AgentRoleCreateDTO;
import cn.lili.modules.agent.entity.enums.AgentLevelEnum;
import cn.lili.modules.agent.entity.enums.AgentStatusEnum;
import cn.lili.modules.agent.entity.vos.AgentRoleRelationVO;
import cn.lili.modules.agent.mapper.AgentRoleRelationMapper;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 代理商角色关系业务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class AgentRoleRelationServiceImpl extends ServiceImpl<AgentRoleRelationMapper, AgentRoleRelation> implements AgentRoleRelationService {

    private static final String ROLE_AGENT = "ROLE_AGENT";

    @Autowired
    private MemberService memberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRoleRelation createAgentRole(AgentRoleCreateDTO dto) {
        Member member = memberService.getById(dto.getMemberId());
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        boolean exists = this.count(new LambdaQueryWrapper<AgentRoleRelation>()
                .eq(AgentRoleRelation::getMemberId, dto.getMemberId())
                .eq(AgentRoleRelation::getRegionId, dto.getRegionId())
                .eq(AgentRoleRelation::getDeleteFlag, false)) > 0;
        if (exists) {
            throw new ServiceException(ResultCode.AGENT_ROLE_ALREADY_EXISTS);
        }
        AgentRoleRelation relation = buildRelation(dto);
        this.save(relation);
        return relation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRoleRelation ensureAgentRole(AgentRoleCreateDTO dto) {
        Member member = memberService.getById(dto.getMemberId());
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        validateAgentLevel(dto.getAgentLevel());
        AgentRoleRelation existing = this.getOne(new LambdaQueryWrapper<AgentRoleRelation>()
                .eq(AgentRoleRelation::getMemberId, dto.getMemberId())
                .eq(AgentRoleRelation::getDeleteFlag, false)
                .orderByDesc(AgentRoleRelation::getCreateTime)
                .last("limit 1"), false);
        if (existing != null) {
            existing.setRoleCode(ROLE_AGENT);
            existing.setRegionId(dto.getRegionId());
            existing.setRegionName(dto.getRegionName());
            existing.setAgentLevel(dto.getAgentLevel().trim().toUpperCase(Locale.ROOT));
            existing.setStatus(AgentStatusEnum.ENABLE.name());
            if (StringUtils.isNotBlank(dto.getRemark())) {
                existing.setRemark(dto.getRemark());
            }
            if (existing.getEffectiveTime() == null) {
                existing.setEffectiveTime(new Date());
            }
            this.updateById(existing);
            return existing;
        }
        AgentRoleRelation relation = buildRelation(dto);
        this.save(relation);
        return relation;
    }

    @Override
    public List<AgentRoleRelationVO> listAgentRoles() {
        return this.baseMapper.listAgentRoles();
    }

    @Override
    public AgentRoleRelation getEnabledRoleByMemberId(String memberId) {
        if (StringUtils.isBlank(memberId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<AgentRoleRelation>()
                .eq(AgentRoleRelation::getMemberId, memberId)
                .eq(AgentRoleRelation::getStatus, AgentStatusEnum.ENABLE.name())
                .eq(AgentRoleRelation::getDeleteFlag, false)
                .orderByDesc(AgentRoleRelation::getCreateTime)
                .last("limit 1"), false);
    }

    @Override
    public AgentRoleRelation getLatestRoleByMemberId(String memberId) {
        if (StringUtils.isBlank(memberId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<AgentRoleRelation>()
                .eq(AgentRoleRelation::getMemberId, memberId)
                .eq(AgentRoleRelation::getDeleteFlag, false)
                .orderByDesc(AgentRoleRelation::getCreateTime)
                .last("limit 1"), false);
    }

    @Override
    public boolean isAgent(String memberId) {
        return this.count(new LambdaQueryWrapper<AgentRoleRelation>()
                .eq(AgentRoleRelation::getMemberId, memberId)
                .eq(AgentRoleRelation::getStatus, AgentStatusEnum.ENABLE.name())
                .eq(AgentRoleRelation::getDeleteFlag, false)) > 0;
    }

    private AgentRoleRelation buildRelation(AgentRoleCreateDTO dto) {
        validateAgentLevel(dto.getAgentLevel());
        AgentRoleRelation relation = new AgentRoleRelation();
        relation.setMemberId(dto.getMemberId());
        relation.setRoleCode(ROLE_AGENT);
        relation.setRegionId(dto.getRegionId());
        relation.setRegionName(dto.getRegionName());
        relation.setAgentLevel(dto.getAgentLevel().trim().toUpperCase(Locale.ROOT));
        relation.setStatus(AgentStatusEnum.ENABLE.name());
        relation.setRemark(dto.getRemark());
        relation.setEffectiveTime(new Date());
        return relation;
    }

    private void validateAgentLevel(String agentLevel) {
        if (StringUtils.isBlank(agentLevel)) {
            throw new ServiceException(ResultCode.AGENT_LEVEL_REQUIRED);
        }
        try {
            AgentLevelEnum.valueOf(agentLevel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(ResultCode.AGENT_LEVEL_ERROR);
        }
    }
}
