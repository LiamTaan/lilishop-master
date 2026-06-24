package cn.lili.modules.agent.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dos.AgentStoreBind;
import cn.lili.modules.agent.entity.dto.AgentStoreBindAuditDTO;
import cn.lili.modules.agent.entity.dto.AgentStoreBindDTO;
import cn.lili.modules.agent.entity.enums.AgentBindAuditStatusEnum;
import cn.lili.modules.agent.entity.enums.AgentStoreBindStatusEnum;
import cn.lili.modules.agent.entity.params.AgentStoreBindSearchParams;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import cn.lili.modules.agent.mapper.AgentStoreBindMapper;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 代理商店铺绑定业务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class AgentStoreBindServiceImpl extends ServiceImpl<AgentStoreBindMapper, AgentStoreBind> implements AgentStoreBindService {

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private StoreService storeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentStoreBind createBind(AgentStoreBindDTO dto) {
        if (!agentRoleRelationService.isAgent(dto.getAgentMemberId())) {
            throw new ServiceException(ResultCode.AGENT_ROLE_REQUIRED);
        }
        Store store = storeService.getById(dto.getStoreId());
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        if ("CLOSED".equalsIgnoreCase(store.getStoreDisable())) {
            throw new ServiceException(ResultCode.STORE_STATUS_ERROR);
        }
        boolean exists = this.count(new LambdaQueryWrapper<AgentStoreBind>()
                .eq(AgentStoreBind::getAgentMemberId, dto.getAgentMemberId())
                .eq(AgentStoreBind::getStoreId, dto.getStoreId())
                .ne(AgentStoreBind::getBindStatus, AgentStoreBindStatusEnum.UNBOUND.name())
                .eq(AgentStoreBind::getDeleteFlag, false)) > 0;
        if (exists) {
            throw new ServiceException(ResultCode.AGENT_BIND_ALREADY_EXISTS);
        }
        AgentStoreBind bind = this.getOne(new LambdaQueryWrapper<AgentStoreBind>()
                .eq(AgentStoreBind::getAgentMemberId, dto.getAgentMemberId())
                .eq(AgentStoreBind::getStoreId, dto.getStoreId())
                .eq(AgentStoreBind::getDeleteFlag, false)
                .orderByDesc(AgentStoreBind::getCreateTime)
                .last("limit 1"), false);
        boolean reuseHistory = bind != null;
        if (!reuseHistory) {
            bind = new AgentStoreBind();
        }
        bind.setAgentMemberId(dto.getAgentMemberId());
        bind.setStoreId(dto.getStoreId());
        String storeName = StringUtils.defaultIfBlank(store.getStoreName(), store.getMemberName());
        if (StringUtils.isBlank(storeName)) {
            storeName = store.getId();
        }
        bind.setStoreName(storeName);
        bind.setRegionId(dto.getRegionId());
        bind.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());
        bind.setAuditStatus(AgentBindAuditStatusEnum.SUBMITTED.name());
        bind.setAuditRemark(dto.getAuditRemark());
        bind.setRemark(dto.getRemark());
        bind.setBindTime(DateUtil.date());
        bind.setUnbindTime(null);
        if (reuseHistory) {
            this.updateById(bind);
            this.update(new LambdaUpdateWrapper<AgentStoreBind>()
                    .eq(AgentStoreBind::getId, bind.getId())
                    .set(AgentStoreBind::getUnbindTime, null));
        } else {
            this.save(bind);
        }
        return bind;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentStoreBind auditBind(String bindId, AgentStoreBindAuditDTO dto) {
        AgentStoreBind bind = this.getById(bindId);
        if (bind == null || Boolean.TRUE.equals(bind.getDeleteFlag())) {
            throw new ServiceException(ResultCode.AGENT_BIND_NOT_FOUND);
        }
        if (!AgentBindAuditStatusEnum.SUBMITTED.name().equals(bind.getAuditStatus())) {
            throw new ServiceException(ResultCode.AGENT_AUDIT_STATUS_ERROR);
        }
        if (!AgentBindAuditStatusEnum.APPROVED.name().equals(dto.getAuditStatus())
                && !AgentBindAuditStatusEnum.REJECTED.name().equals(dto.getAuditStatus())) {
            throw new ServiceException(ResultCode.AGENT_AUDIT_STATUS_ERROR);
        }
        bind.setAuditStatus(dto.getAuditStatus());
        bind.setAuditRemark(dto.getAuditRemark());
        if (AgentBindAuditStatusEnum.REJECTED.name().equals(dto.getAuditStatus())) {
            bind.setBindStatus(AgentStoreBindStatusEnum.UNBOUND.name());
            bind.setUnbindTime(DateUtil.date());
        } else {
            bind.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());
            bind.setUnbindTime(null);
        }
        this.updateById(bind);
        if (AgentBindAuditStatusEnum.APPROVED.name().equals(dto.getAuditStatus())) {
            this.update(new LambdaUpdateWrapper<AgentStoreBind>()
                    .eq(AgentStoreBind::getId, bind.getId())
                    .set(AgentStoreBind::getUnbindTime, null));
        }
        return bind;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbind(String bindId) {
        AgentStoreBind bind = this.getById(bindId);
        if (bind == null || Boolean.TRUE.equals(bind.getDeleteFlag())) {
            throw new ServiceException(ResultCode.AGENT_BIND_NOT_FOUND);
        }
        if (!AgentBindAuditStatusEnum.APPROVED.name().equalsIgnoreCase(bind.getAuditStatus())
                || !AgentStoreBindStatusEnum.BOUND.name().equalsIgnoreCase(bind.getBindStatus())) {
            throw new ServiceException(ResultCode.AGENT_AUDIT_STATUS_ERROR);
        }
        bind.setBindStatus(AgentStoreBindStatusEnum.UNBOUND.name());
        bind.setUnbindTime(DateUtil.date());
        return this.updateById(bind);
    }

    @Override
    public IPage<AgentStoreBindVO> pageAgentStoreBinds(AgentStoreBindSearchParams searchParams) {
        AgentStoreBindSearchParams query = searchParams == null ? new AgentStoreBindSearchParams() : searchParams;
        String bindStatus = StringUtils.isNotBlank(query.getBindStatus()) ? query.getBindStatus().toUpperCase(Locale.ROOT) : null;
        String auditStatus = StringUtils.isNotBlank(query.getAuditStatus()) ? query.getAuditStatus().toUpperCase(Locale.ROOT) : null;
        List<AgentStoreBindVO> filtered = this.baseMapper.listAgentStoreBinds().stream()
                .filter(item -> StringUtils.isBlank(query.getAgentMemberId()) || StringUtils.equals(item.getAgentMemberId(), query.getAgentMemberId()))
                .filter(item -> StringUtils.isBlank(query.getStoreId()) || StringUtils.equals(item.getStoreId(), query.getStoreId()))
                .filter(item -> StringUtils.isBlank(query.getRegionId()) || StringUtils.equals(item.getRegionId(), query.getRegionId()))
                .filter(item -> StringUtils.isBlank(bindStatus) || StringUtils.equalsIgnoreCase(item.getBindStatus(), bindStatus))
                .filter(item -> StringUtils.isBlank(auditStatus) || StringUtils.equalsIgnoreCase(item.getAuditStatus(), auditStatus))
                .filter(item -> StringUtils.isBlank(query.getKeyword())
                        || StringUtils.containsIgnoreCase(item.getStoreName(), query.getKeyword())
                        || StringUtils.containsIgnoreCase(item.getAgentMemberId(), query.getKeyword())
                        || StringUtils.containsIgnoreCase(item.getAgentMemberName(), query.getKeyword()))
                .collect(Collectors.toList());

        Page<AgentStoreBindVO> page = PageUtil.initPage(query);
        int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
        if (fromIndex >= filtered.size()) {
            page.setRecords(Collections.emptyList());
            page.setTotal(filtered.size());
            return page;
        }
        int toIndex = (int) Math.min(fromIndex + page.getSize(), filtered.size());
        page.setRecords(filtered.subList(fromIndex, toIndex));
        page.setTotal(filtered.size());
        return page;
    }

    @Override
    public List<AgentStoreBindVO> listApprovedBindsByAgentMemberId(String agentMemberId) {
        List<AgentStoreBindVO> binds = new ArrayList<>(this.baseMapper.listApprovedBindsByAgentMemberId(agentMemberId));
        Store ownStore = storeService.getStoreByMemberId(agentMemberId);
        if (ownStore != null
                && StoreAuditStatusEnum.APPROVED.name().equalsIgnoreCase(ownStore.getAuditStatus())
                && StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(ownStore.getStoreType())) {
            boolean exists = binds.stream().anyMatch(item -> ownStore.getId().equals(item.getStoreId()));
            if (!exists) {
                binds.add(buildSelfOwnedBind(ownStore));
            }
        }
        return binds;
    }

    @Override
    public List<String> listApprovedStoreIdsByAgentMemberId(String agentMemberId) {
        List<AgentStoreBindVO> binds = this.listApprovedBindsByAgentMemberId(agentMemberId);
        if (binds == null || binds.isEmpty()) {
            return Collections.emptyList();
        }
        return binds.stream().map(AgentStoreBindVO::getStoreId).distinct().collect(Collectors.toList());
    }

    private AgentStoreBindVO buildSelfOwnedBind(Store ownStore) {
        AgentStoreBindVO vo = new AgentStoreBindVO();
        vo.setId("SELF-" + ownStore.getId());
        vo.setAgentMemberId(ownStore.getMemberId());
        vo.setAgentMemberName(ownStore.getMemberName());
        vo.setStoreId(ownStore.getId());
        vo.setStoreName(ownStore.getStoreName());
        vo.setRegionId(ownStore.getAgentRegionId());
        vo.setRegionName(ownStore.getAgentRegionName());
        vo.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());
        vo.setAuditStatus(AgentBindAuditStatusEnum.APPROVED.name());
        vo.setAuditRemark("代理商自有店铺");
        vo.setRemark("入驻审核通过自动生效");
        vo.setBindTime(ownStore.getUpdateTime());
        return vo;
    }
}
