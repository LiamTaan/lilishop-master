package cn.lili.modules.member.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.enums.AgentStatusEnum;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.member.entity.dos.Clerk;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.LoginSessionDTO;
import cn.lili.modules.member.entity.enums.LoginAuthDomainEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityNextActionEnum;
import cn.lili.modules.member.entity.enums.LoginIdentityStatusEnum;
import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import cn.lili.modules.member.entity.vo.LoginIdentitySelectionResultVO;
import cn.lili.modules.member.entity.vo.LoginIdentitySummaryVO;
import cn.lili.modules.member.entity.vo.LoginSessionSnapshotVO;
import cn.lili.modules.member.entity.vo.MemberVO;
import cn.lili.modules.member.service.AppLoginIdentityService;
import cn.lili.modules.member.service.AppLoginSessionService;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * App 登录身份服务实现
 *
 * @author OpenAI
 */
@Service
public class AppLoginIdentityServiceImpl implements AppLoginIdentityService {

    private static final EnumSet<LoginIdentityCodeEnum> MEMBER_IDENTITIES = EnumSet.of(LoginIdentityCodeEnum.CONSUMER, LoginIdentityCodeEnum.AGENT);
    private static final EnumSet<LoginIdentityCodeEnum> STORE_IDENTITIES = EnumSet.of(LoginIdentityCodeEnum.SUPPLIER);

    @Autowired
    private MemberService memberService;
    @Autowired
    private AgentRoleRelationService agentRoleRelationService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private ClerkService clerkService;
    @Autowired
    private AppLoginSessionService appLoginSessionService;

    @Override
    public List<LoginIdentityOptionVO> resolveIdentityOptions(Member member) {
        List<LoginIdentityOptionVO> options = new ArrayList<>();
        options.add(buildConsumerOption());
        options.add(buildAgentOption(member));
        options.add(buildSupplierOption(member));
        return options;
    }

    @Override
    public LoginSessionSnapshotVO createLoginSession(Member member, String mobile, LoginSessionChannelEnum channel) {
        List<LoginIdentityOptionVO> identityOptions = resolveIdentityOptions(member);
        return appLoginSessionService.createSession(member.getId(), mobile, channel, identityOptions);
    }

    @Override
    public LoginSessionSnapshotVO getLoginSessionSnapshot(String loginSessionToken) {
        LoginSessionDTO session = appLoginSessionService.getSession(loginSessionToken);
        Member member = getMemberOrThrow(session.getMemberId());
        return createSnapshot(session, resolveIdentityOptions(member));
    }

    @Override
    public LoginIdentitySelectionResultVO issueMemberToken(String loginSessionToken, LoginIdentityCodeEnum identityCode) {
        if (!MEMBER_IDENTITIES.contains(identityCode)) {
            throw new ServiceException(ResultCode.IDENTITY_NOT_SUPPORTED);
        }
        LoginSessionDTO session = appLoginSessionService.getSession(loginSessionToken);
        Member member = getMemberOrThrow(session.getMemberId());
        List<LoginIdentityOptionVO> currentOptions = resolveIdentityOptions(member);
        LoginIdentityOptionVO option = findOption(currentOptions, identityCode);
        validateAvailable(option);
        LoginIdentitySelectionResultVO result = new LoginIdentitySelectionResultVO();
        result.setIdentityCode(identityCode);
        result.setAuthDomain(LoginAuthDomainEnum.MEMBER);
        result.setToken(memberService.mobilePhoneLogin(session.getMobile(), identityCode));
        result.setMemberProfile(memberService.getMember(member.getId()));
        result.setIdentityStatusSummary(buildSummary(currentOptions));
        return result;
    }

    @Override
    public LoginIdentitySelectionResultVO issueStoreToken(String loginSessionToken, LoginIdentityCodeEnum identityCode) {
        if (!STORE_IDENTITIES.contains(identityCode)) {
            throw new ServiceException(ResultCode.IDENTITY_NOT_SUPPORTED);
        }
        LoginSessionDTO session = appLoginSessionService.getSession(loginSessionToken);
        Member member = getMemberOrThrow(session.getMemberId());
        List<LoginIdentityOptionVO> currentOptions = resolveIdentityOptions(member);
        LoginIdentityOptionVO option = findOption(currentOptions, identityCode);
        validateAvailable(option);
        LoginIdentitySelectionResultVO result = new LoginIdentitySelectionResultVO();
        result.setIdentityCode(identityCode);
        result.setAuthDomain(LoginAuthDomainEnum.STORE);
        result.setToken(memberService.mobilePhoneStoreLogin(session.getMobile()));
        result.setMemberProfile(memberService.getMember(member.getId()));
        result.setIdentityStatusSummary(buildSummary(currentOptions));
        return result;
    }

    private LoginSessionSnapshotVO createSnapshot(LoginSessionDTO session, List<LoginIdentityOptionVO> identityOptions) {
        LoginSessionSnapshotVO snapshot = new LoginSessionSnapshotVO();
        snapshot.setLoginSessionToken(session.getToken());
        snapshot.setMemberId(session.getMemberId());
        snapshot.setMobile(session.getMobile());
        snapshot.setChannel(session.getChannel());
        snapshot.setIdentityOptions(identityOptions);
        return snapshot;
    }

    private Member getMemberOrThrow(String memberId) {
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        return member;
    }

    private LoginIdentityOptionVO buildConsumerOption() {
        LoginIdentityOptionVO option = new LoginIdentityOptionVO();
        option.setIdentityCode(LoginIdentityCodeEnum.CONSUMER);
        option.setAuthDomain(LoginAuthDomainEnum.MEMBER);
        option.setAvailable(true);
        option.setStatus(LoginIdentityStatusEnum.AVAILABLE);
        option.setMessage("可直接进入消费者身份");
        option.setNextAction(LoginIdentityNextActionEnum.NONE);
        return option;
    }

    private LoginIdentityOptionVO buildAgentOption(Member member) {
        LoginIdentityOptionVO option = new LoginIdentityOptionVO();
        option.setIdentityCode(LoginIdentityCodeEnum.AGENT);
        option.setAuthDomain(LoginAuthDomainEnum.MEMBER);
        Store store = storeService.getStoreByMemberId(member.getId());
        if (store == null || !StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(store.getStoreType())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.NOT_OPENED);
            option.setMessage("当前账号尚未开通代理商身份");
            option.setNextAction(LoginIdentityNextActionEnum.APPLY_AGENT);
            return option;
        }
        if (StoreAuditStatusEnum.REJECTED.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreStatusEnum.REFUSED.name().equalsIgnoreCase(store.getStoreDisable())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.REJECTED);
            option.setMessage("代理商资质审核未通过，请重新提交");
            option.setNextAction(LoginIdentityNextActionEnum.RESUBMIT);
            return option;
        }
        if (StoreAuditStatusEnum.SUBMITTED.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreAuditStatusEnum.DRAFT.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreStatusEnum.APPLY.name().equalsIgnoreCase(store.getStoreDisable())
                || StoreStatusEnum.APPLYING.name().equalsIgnoreCase(store.getStoreDisable())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.PENDING);
            option.setMessage("代理商资质审核中，请等待审核结果");
            option.setNextAction(LoginIdentityNextActionEnum.NONE);
            return option;
        }
        AgentRoleRelation enabledRole = agentRoleRelationService.getEnabledRoleByMemberId(member.getId());
        if (enabledRole != null) {
            option.setAvailable(true);
            option.setStatus(LoginIdentityStatusEnum.AVAILABLE);
            option.setMessage("可直接进入代理商身份");
            option.setNextAction(LoginIdentityNextActionEnum.NONE);
            return option;
        }
        AgentRoleRelation latestRole = agentRoleRelationService.getLatestRoleByMemberId(member.getId());
        if (latestRole != null && AgentStatusEnum.DISABLE.name().equalsIgnoreCase(latestRole.getStatus())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.DISABLED);
            option.setMessage("代理商身份已禁用，请联系管理员");
            option.setNextAction(LoginIdentityNextActionEnum.CONTACT_ADMIN);
            return option;
        }
        option.setAvailable(false);
        option.setStatus(LoginIdentityStatusEnum.DISABLED);
        option.setMessage("代理商账号未初始化，请联系管理员");
        option.setNextAction(LoginIdentityNextActionEnum.CONTACT_ADMIN);
        return option;
    }

    private LoginIdentityOptionVO buildSupplierOption(Member member) {
        LoginIdentityOptionVO option = new LoginIdentityOptionVO();
        option.setIdentityCode(LoginIdentityCodeEnum.SUPPLIER);
        option.setAuthDomain(LoginAuthDomainEnum.STORE);
        Store store = storeService.getStoreByMemberId(member.getId());
        if (store == null || !StoreBizTypeEnum.SUPPLIER.name().equalsIgnoreCase(store.getStoreType())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.NOT_OPENED);
            option.setMessage("当前账号尚未开通供货商身份");
            option.setNextAction(LoginIdentityNextActionEnum.APPLY_STORE);
            return option;
        }
        if (StoreAuditStatusEnum.REJECTED.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreStatusEnum.REFUSED.name().equalsIgnoreCase(store.getStoreDisable())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.REJECTED);
            option.setMessage("供货商资质审核未通过，请重新提交");
            option.setNextAction(LoginIdentityNextActionEnum.RESUBMIT);
            return option;
        }
        if (StoreAuditStatusEnum.SUBMITTED.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreAuditStatusEnum.DRAFT.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreStatusEnum.APPLY.name().equalsIgnoreCase(store.getStoreDisable())
                || StoreStatusEnum.APPLYING.name().equalsIgnoreCase(store.getStoreDisable())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.PENDING);
            option.setMessage("供货商资质审核中，请等待审核结果");
            option.setNextAction(LoginIdentityNextActionEnum.NONE);
            return option;
        }
        Clerk clerk = clerkService.getClerkByMemberId(member.getId());
        if (clerk == null) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.DISABLED);
            option.setMessage("供货商账号未初始化，请联系管理员");
            option.setNextAction(LoginIdentityNextActionEnum.CONTACT_ADMIN);
            return option;
        }
        if (Boolean.FALSE.equals(clerk.getStatus())
                || StoreAuditStatusEnum.FROZEN.name().equalsIgnoreCase(store.getAuditStatus())
                || StoreStatusEnum.CLOSED.name().equalsIgnoreCase(store.getStoreDisable())) {
            option.setAvailable(false);
            option.setStatus(LoginIdentityStatusEnum.DISABLED);
            option.setMessage("供货商账号已禁用，请联系管理员");
            option.setNextAction(LoginIdentityNextActionEnum.CONTACT_ADMIN);
            return option;
        }
        option.setAvailable(true);
        option.setStatus(LoginIdentityStatusEnum.AVAILABLE);
        option.setMessage("可直接进入供货商身份");
        option.setNextAction(LoginIdentityNextActionEnum.NONE);
        return option;
    }

    private LoginIdentityOptionVO findOption(List<LoginIdentityOptionVO> options, LoginIdentityCodeEnum identityCode) {
        return options.stream()
                .filter(option -> option.getIdentityCode() == identityCode)
                .findFirst()
                .orElseThrow(() -> new ServiceException(ResultCode.IDENTITY_NOT_SUPPORTED));
    }

    private void validateAvailable(LoginIdentityOptionVO option) {
        if (Boolean.TRUE.equals(option.getAvailable())) {
            return;
        }
        if (option.getIdentityCode() == LoginIdentityCodeEnum.SUPPLIER) {
            switch (option.getStatus()) {
                case PENDING -> throw new ServiceException(ResultCode.STORE_AUDIT_PENDING);
                case REJECTED -> throw new ServiceException(ResultCode.STORE_AUDIT_REJECTED);
                case DISABLED -> throw new ServiceException(ResultCode.STORE_ACCOUNT_DISABLED);
                case NOT_OPENED -> throw new ServiceException(ResultCode.STORE_ACCOUNT_NOT_FOUND);
                default -> throw new ServiceException(ResultCode.IDENTITY_NOT_AVAILABLE, option.getMessage());
            }
        }
        if (option.getIdentityCode() == LoginIdentityCodeEnum.AGENT) {
            switch (option.getStatus()) {
                case PENDING -> throw new ServiceException(ResultCode.AGENT_AUDIT_PENDING);
                case REJECTED -> throw new ServiceException(ResultCode.AGENT_AUDIT_REJECTED);
                default -> throw new ServiceException(ResultCode.IDENTITY_NOT_AVAILABLE, option.getMessage());
            }
        }
        throw new ServiceException(ResultCode.IDENTITY_NOT_AVAILABLE, option.getMessage());
    }

    private LoginIdentitySummaryVO buildSummary(List<LoginIdentityOptionVO> currentOptions) {
        LoginIdentitySummaryVO summary = new LoginIdentitySummaryVO();
        summary.setIdentityOptions(currentOptions);
        return summary;
    }
}
