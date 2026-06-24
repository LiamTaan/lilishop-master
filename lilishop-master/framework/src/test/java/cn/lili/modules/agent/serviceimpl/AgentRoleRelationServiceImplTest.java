package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.enums.AgentLevelEnum;
import cn.lili.modules.agent.entity.dto.AgentRoleCreateDTO;
import cn.lili.modules.agent.entity.enums.AgentStatusEnum;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.service.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRoleRelationServiceImplTest {

    @Spy
    @InjectMocks
    private AgentRoleRelationServiceImpl agentRoleRelationService;

    @Mock
    private MemberService memberService;

    @Test
    void createAgentRoleShouldRejectDuplicateRole() {
        AgentRoleCreateDTO dto = new AgentRoleCreateDTO();
        dto.setMemberId("member-1");
        dto.setRegionId("110000");
        dto.setAgentLevel(AgentLevelEnum.CITY.name());

        when(memberService.getById("member-1")).thenReturn(new Member());
        doReturn(1L).when(agentRoleRelationService).count(any());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> agentRoleRelationService.createAgentRole(dto));

        Assertions.assertEquals(ResultCode.AGENT_ROLE_ALREADY_EXISTS, exception.getResultCode());
    }

    @Test
    void createAgentRoleShouldInitializeEnabledAgentRole() {
        AgentRoleCreateDTO dto = new AgentRoleCreateDTO();
        dto.setMemberId("member-1");
        dto.setRegionId("110000");
        dto.setRegionName("北京");
        dto.setAgentLevel(AgentLevelEnum.CITY.name());
        dto.setRemark("agent");

        when(memberService.getById("member-1")).thenReturn(new Member());
        doReturn(0L).when(agentRoleRelationService).count(any());
        doReturn(true).when(agentRoleRelationService).save(any(AgentRoleRelation.class));

        AgentRoleRelation relation = agentRoleRelationService.createAgentRole(dto);

        Assertions.assertEquals("member-1", relation.getMemberId());
        Assertions.assertEquals("110000", relation.getRegionId());
        Assertions.assertEquals("北京", relation.getRegionName());
        Assertions.assertEquals(AgentLevelEnum.CITY.name(), relation.getAgentLevel());
        Assertions.assertEquals(AgentStatusEnum.ENABLE.name(), relation.getStatus());
        Assertions.assertEquals("ROLE_AGENT", relation.getRoleCode());
        Assertions.assertNotNull(relation.getEffectiveTime());
        verify(agentRoleRelationService).save(any(AgentRoleRelation.class));
    }
}
