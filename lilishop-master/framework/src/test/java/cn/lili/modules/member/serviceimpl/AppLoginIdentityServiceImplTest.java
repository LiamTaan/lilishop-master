package cn.lili.modules.member.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.token.Token;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.enums.AgentStatusEnum;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.LoginSessionDTO;
import cn.lili.modules.member.entity.enums.LoginAuthDomainEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginIdentitySelectionResultVO;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import cn.lili.modules.member.entity.vo.MemberVO;
import cn.lili.modules.member.service.AppLoginSessionService;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AppLoginIdentityServiceImplTest {

    @InjectMocks
    private AppLoginIdentityServiceImpl appLoginIdentityService;

    @Mock
    private MemberService memberService;

    @Mock
    private AgentRoleRelationService agentRoleRelationService;

    @Mock
    private StoreService storeService;

    @Mock
    private ClerkService clerkService;

    @Mock
    private AppLoginSessionService appLoginSessionService;

    @Test
    void issueMemberTokenShouldReturnMemberTokenForConsumer() {
        LoginSessionDTO session = new LoginSessionDTO();
        session.setToken("session-1");
        session.setMemberId("member-1");
        session.setMobile("13900000000");
        session.setChannel(LoginSessionChannelEnum.SMS);

        Member member = new Member();
        member.setId("member-1");
        member.setUsername("user1");
        member.setMobile("13900000000");
        member.setDisabled(true);

        Token token = new Token();
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");

        MemberVO memberVO = new MemberVO();
        memberVO.setId("member-1");
        memberVO.setMobile("13900000000");

        doReturn(session).when(appLoginSessionService).getSession("session-1");
        doReturn(member).when(memberService).getById("member-1");
        doReturn(token).when(memberService).mobilePhoneLogin("13900000000", LoginIdentityCodeEnum.CONSUMER);
        doReturn(memberVO).when(memberService).getMember("member-1");

        LoginIdentitySelectionResultVO result = appLoginIdentityService.issueMemberToken("session-1", LoginIdentityCodeEnum.CONSUMER);

        Assertions.assertEquals(LoginIdentityCodeEnum.CONSUMER, result.getIdentityCode());
        Assertions.assertEquals(LoginAuthDomainEnum.MEMBER, result.getAuthDomain());
        Assertions.assertEquals("access-token", result.getToken().getAccessToken());
        Assertions.assertEquals("member-1", result.getMemberProfile().getId());
        Assertions.assertNotNull(result.getIdentityStatusSummary());
        Assertions.assertEquals(3, result.getIdentityStatusSummary().getIdentityOptions().size());
    }

    @Test
    void issueMemberTokenShouldReturnMemberTokenForAgentIdentity() {
        LoginSessionDTO session = new LoginSessionDTO();
        session.setToken("session-agent-1");
        session.setMemberId("member-agent-1");
        session.setMobile("13600000000");
        session.setChannel(LoginSessionChannelEnum.SMS);

        Member member = new Member();
        member.setId("member-agent-1");
        member.setUsername("agent-user");
        member.setMobile("13600000000");
        member.setDisabled(true);

        Store store = new Store();
        store.setMemberId("member-agent-1");
        store.setStoreType(StoreBizTypeEnum.AGENT.name());
        store.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());
        store.setStoreDisable(StoreStatusEnum.OPEN.name());

        AgentRoleRelation enabledRole = new AgentRoleRelation();
        enabledRole.setMemberId("member-agent-1");
        enabledRole.setStatus(AgentStatusEnum.ENABLE.name());

        Token token = new Token();
        token.setAccessToken("agent-access-token");
        token.setRefreshToken("agent-refresh-token");

        MemberVO memberVO = new MemberVO();
        memberVO.setId("member-agent-1");

        doReturn(session).when(appLoginSessionService).getSession("session-agent-1");
        doReturn(member).when(memberService).getById("member-agent-1");
        doReturn(store).when(storeService).getStoreByMemberId("member-agent-1");
        doReturn(enabledRole).when(agentRoleRelationService).getEnabledRoleByMemberId("member-agent-1");
        doReturn(token).when(memberService).mobilePhoneLogin("13600000000", LoginIdentityCodeEnum.AGENT);
        doReturn(memberVO).when(memberService).getMember("member-agent-1");

        LoginIdentitySelectionResultVO result = appLoginIdentityService.issueMemberToken("session-agent-1", LoginIdentityCodeEnum.AGENT);

        Assertions.assertEquals(LoginIdentityCodeEnum.AGENT, result.getIdentityCode());
        Assertions.assertEquals("agent-access-token", result.getToken().getAccessToken());
    }

    @Test
    void issueStoreTokenShouldRejectWhenSupplierIdentityIsUnavailable() {
        LoginSessionDTO session = new LoginSessionDTO();
        session.setToken("session-2");
        session.setMemberId("member-2");
        session.setMobile("13800000000");
        session.setChannel(LoginSessionChannelEnum.ONE_CLICK);

        Member member = new Member();
        member.setId("member-2");
        member.setUsername("user2");
        member.setMobile("13800000000");
        member.setDisabled(true);

        doReturn(session).when(appLoginSessionService).getSession("session-2");
        doReturn(member).when(memberService).getById("member-2");
        doReturn(null).when(storeService).getStoreByMemberId("member-2");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> appLoginIdentityService.issueStoreToken("session-2", LoginIdentityCodeEnum.SUPPLIER));

        Assertions.assertEquals(ResultCode.STORE_ACCOUNT_NOT_FOUND, exception.getResultCode());
    }

    @Test
    void resolveIdentityOptionsShouldMarkAgentPendingWhenAgentStoreIsUnderReview() {
        Member member = new Member();
        member.setId("member-agent-pending");

        Store store = new Store();
        store.setMemberId("member-agent-pending");
        store.setStoreType(StoreBizTypeEnum.AGENT.name());
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
        store.setStoreDisable(StoreStatusEnum.APPLYING.name());

        doReturn(store).when(storeService).getStoreByMemberId("member-agent-pending");

        LoginIdentityOptionVO option = appLoginIdentityService.resolveIdentityOptions(member).stream()
                .filter(item -> item.getIdentityCode() == LoginIdentityCodeEnum.AGENT)
                .findFirst()
                .orElseThrow();

        Assertions.assertFalse(option.getAvailable());
        Assertions.assertEquals(cn.lili.modules.member.entity.enums.LoginIdentityStatusEnum.PENDING, option.getStatus());
    }

    @Test
    void issueMemberTokenShouldRejectAgentWhenAgentStoreAuditRejected() {
        LoginSessionDTO session = new LoginSessionDTO();
        session.setToken("session-agent-rejected");
        session.setMemberId("member-agent-rejected");
        session.setMobile("13700000000");
        session.setChannel(LoginSessionChannelEnum.SMS);

        Member member = new Member();
        member.setId("member-agent-rejected");

        Store store = new Store();
        store.setMemberId("member-agent-rejected");
        store.setStoreType(StoreBizTypeEnum.AGENT.name());
        store.setAuditStatus(StoreAuditStatusEnum.REJECTED.name());
        store.setStoreDisable(StoreStatusEnum.REFUSED.name());

        doReturn(session).when(appLoginSessionService).getSession("session-agent-rejected");
        doReturn(member).when(memberService).getById("member-agent-rejected");
        doReturn(store).when(storeService).getStoreByMemberId("member-agent-rejected");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> appLoginIdentityService.issueMemberToken("session-agent-rejected", LoginIdentityCodeEnum.AGENT));

        Assertions.assertEquals(ResultCode.AGENT_AUDIT_REJECTED, exception.getResultCode());
    }

    @Test
    void resolveIdentityOptionsShouldMarkAgentDisabledWhenRoleIsDisabledAfterApproval() {
        Member member = new Member();
        member.setId("member-agent-disabled");

        Store store = new Store();
        store.setMemberId("member-agent-disabled");
        store.setStoreType(StoreBizTypeEnum.AGENT.name());
        store.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());
        store.setStoreDisable(StoreStatusEnum.OPEN.name());

        AgentRoleRelation disabledRole = new AgentRoleRelation();
        disabledRole.setMemberId("member-agent-disabled");
        disabledRole.setStatus(AgentStatusEnum.DISABLE.name());

        doReturn(store).when(storeService).getStoreByMemberId("member-agent-disabled");
        doReturn(null).when(agentRoleRelationService).getEnabledRoleByMemberId("member-agent-disabled");
        doReturn(disabledRole).when(agentRoleRelationService).getLatestRoleByMemberId("member-agent-disabled");

        LoginIdentityOptionVO option = appLoginIdentityService.resolveIdentityOptions(member).stream()
                .filter(item -> item.getIdentityCode() == LoginIdentityCodeEnum.AGENT)
                .findFirst()
                .orElseThrow();

        Assertions.assertFalse(option.getAvailable());
        Assertions.assertEquals(cn.lili.modules.member.entity.enums.LoginIdentityStatusEnum.DISABLED, option.getStatus());
    }
}
