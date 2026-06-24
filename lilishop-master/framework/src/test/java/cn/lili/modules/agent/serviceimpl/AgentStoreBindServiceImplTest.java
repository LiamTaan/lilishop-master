package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dos.AgentStoreBind;
import cn.lili.modules.agent.entity.dto.AgentStoreBindAuditDTO;
import cn.lili.modules.agent.entity.dto.AgentStoreBindDTO;
import cn.lili.modules.agent.entity.enums.AgentBindAuditStatusEnum;
import cn.lili.modules.agent.entity.enums.AgentStoreBindStatusEnum;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.service.StoreService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentStoreBindServiceImplTest {

    @Spy
    @InjectMocks
    private AgentStoreBindServiceImpl agentStoreBindService;

    @Mock
    private AgentRoleRelationService agentRoleRelationService;

    @Mock
    private StoreService storeService;

    @Test
    void createBindShouldRejectDuplicateBinding() {
        AgentStoreBindDTO dto = new AgentStoreBindDTO();
        dto.setAgentMemberId("agent-1");
        dto.setStoreId("store-1");

        when(agentRoleRelationService.isAgent("agent-1")).thenReturn(true);
        when(storeService.getById("store-1")).thenReturn(new Store());
        doReturn(1L).when(agentStoreBindService).count(any());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> agentStoreBindService.createBind(dto));

        Assertions.assertEquals(ResultCode.AGENT_BIND_ALREADY_EXISTS, exception.getResultCode());
    }

    @Test
    void createBindShouldInitializeSubmittedBinding() {
        AgentStoreBindDTO dto = new AgentStoreBindDTO();
        dto.setAgentMemberId("agent-1");
        dto.setStoreId("store-1");
        dto.setRegionId("110000");
        dto.setAuditRemark("submit");
        dto.setRemark("remark");

        Store store = new Store();
        store.setStoreName("demo-store");

        when(agentRoleRelationService.isAgent("agent-1")).thenReturn(true);
        when(storeService.getById("store-1")).thenReturn(store);
        doReturn(0L).when(agentStoreBindService).count(any());
        doReturn(null).when(agentStoreBindService).getOne(any(), eq(false));
        doReturn(true).when(agentStoreBindService).save(any(AgentStoreBind.class));

        AgentStoreBind bind = agentStoreBindService.createBind(dto);

        Assertions.assertEquals("agent-1", bind.getAgentMemberId());
        Assertions.assertEquals("store-1", bind.getStoreId());
        Assertions.assertEquals("demo-store", bind.getStoreName());
        Assertions.assertEquals(AgentStoreBindStatusEnum.BOUND.name(), bind.getBindStatus());
        Assertions.assertEquals(AgentBindAuditStatusEnum.SUBMITTED.name(), bind.getAuditStatus());
        verify(agentStoreBindService).save(any(AgentStoreBind.class));
    }

    @Test
    void auditBindShouldRejectUnexpectedStatus() {
        AgentStoreBind bind = new AgentStoreBind();
        bind.setId("bind-1");
        bind.setAuditStatus(AgentBindAuditStatusEnum.APPROVED.name());

        AgentStoreBindAuditDTO dto = new AgentStoreBindAuditDTO();
        dto.setAuditStatus(AgentBindAuditStatusEnum.REJECTED.name());

        doReturn(bind).when(agentStoreBindService).getById("bind-1");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> agentStoreBindService.auditBind("bind-1", dto));

        Assertions.assertEquals(ResultCode.AGENT_AUDIT_STATUS_ERROR, exception.getResultCode());
    }

    @Test
    void auditBindShouldMarkRejectedBindingAsUnbound() {
        AgentStoreBind bind = new AgentStoreBind();
        bind.setId("bind-1");
        bind.setAuditStatus(AgentBindAuditStatusEnum.SUBMITTED.name());
        bind.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());

        AgentStoreBindAuditDTO dto = new AgentStoreBindAuditDTO();
        dto.setAuditStatus(AgentBindAuditStatusEnum.REJECTED.name());
        dto.setAuditRemark("reject");

        doReturn(bind).when(agentStoreBindService).getById("bind-1");
        doReturn(true).when(agentStoreBindService).updateById(bind);

        AgentStoreBind result = agentStoreBindService.auditBind("bind-1", dto);

        Assertions.assertEquals(AgentBindAuditStatusEnum.REJECTED.name(), result.getAuditStatus());
        Assertions.assertEquals(AgentStoreBindStatusEnum.UNBOUND.name(), result.getBindStatus());
        Assertions.assertNotNull(result.getUnbindTime());
        verify(agentStoreBindService).updateById(bind);
    }

    @Test
    void unbindShouldRejectSubmittedBinding() {
        AgentStoreBind bind = new AgentStoreBind();
        bind.setId("bind-1");
        bind.setAuditStatus(AgentBindAuditStatusEnum.SUBMITTED.name());
        bind.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());

        doReturn(bind).when(agentStoreBindService).getById("bind-1");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> agentStoreBindService.unbind("bind-1"));

        Assertions.assertEquals(ResultCode.AGENT_AUDIT_STATUS_ERROR, exception.getResultCode());
    }

    @Test
    void unbindShouldMarkApprovedBindingAsUnbound() {
        AgentStoreBind bind = new AgentStoreBind();
        bind.setId("bind-1");
        bind.setAuditStatus(AgentBindAuditStatusEnum.APPROVED.name());
        bind.setBindStatus(AgentStoreBindStatusEnum.BOUND.name());

        doReturn(bind).when(agentStoreBindService).getById("bind-1");
        doReturn(true).when(agentStoreBindService).updateById(bind);

        boolean result = agentStoreBindService.unbind("bind-1");

        Assertions.assertTrue(result);
        Assertions.assertEquals(AgentStoreBindStatusEnum.UNBOUND.name(), bind.getBindStatus());
        Assertions.assertNotNull(bind.getUnbindTime());
        verify(agentStoreBindService).updateById(bind);
    }
}
