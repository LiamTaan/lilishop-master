package cn.lili.modules.store.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.member.entity.dos.Clerk;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.ClerkAddDTO;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dos.StoreDetail;
import cn.lili.modules.store.entity.dto.StoreApplyTypeSelectDTO;
import cn.lili.modules.store.entity.dto.StoreAuditDTO;
import cn.lili.modules.store.entity.dto.StorePersonalApplyDTO;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreAuditLogService;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.system.entity.dos.Region;
import cn.lili.modules.system.service.RegionService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Store.class);
        TableInfoHelper.initTableInfo(assistant, StoreDetail.class);
    }

    @Spy
    @InjectMocks
    private StoreServiceImpl storeService;

    @Mock
    private MemberService memberService;

    @Mock
    private ClerkService clerkService;

    @Mock
    private StoreDetailService storeDetailService;

    @Mock
    private StoreAuditLogService storeAuditLogService;

    @Mock
    private GoodsService goodsService;

    @Mock
    private AgentRoleRelationService agentRoleRelationService;

    @Mock
    private RegionService regionService;

    @Mock
    private Cache cache;

    @Mock
    private SmsUtil smsUtil;

    @Test
    void auditShouldRejectUnsupportedAuditStatus() {
        Store store = new Store();
        store.setId("store-1");
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());

        StoreAuditDTO dto = new StoreAuditDTO();
        dto.setAuditStatus("INVALID");

        doReturn(store).when(storeService).getById("store-1");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> storeService.audit("store-1", dto));

        Assertions.assertEquals(ResultCode.STORE_AUDIT_STATUS_ERROR, exception.getResultCode());
    }

    @Test
    void auditShouldApproveStoreAndInitializeMemberRelationship() {
        Store store = new Store();
        store.setId("store-1");
        store.setMemberId("member-1");
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());
        store.setStoreType(StoreBizTypeEnum.AGENT.name());

        Member member = new Member();
        member.setId("member-1");

        StoreAuditDTO dto = new StoreAuditDTO();
        dto.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());
        dto.setAuditRemark("approved");
        dto.setAgentLevel("CITY");
        dto.setAgentRegionId("110100");
        dto.setAgentRegionName("北京市");

        StoreDetail detail = new StoreDetail();
        detail.setStoreId("store-1");

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).updateById(store);
        when(memberService.getById("member-1")).thenReturn(member);
        when(memberService.updateById(member)).thenReturn(true);
        when(clerkService.saveClerk(any(ClerkAddDTO.class))).thenReturn(new Clerk());
        when(storeAuditLogService.save(any())).thenReturn(true);
        when(storeDetailService.getStoreDetail("store-1")).thenReturn(detail);
        when(storeDetailService.saveOrUpdate(any(StoreDetail.class))).thenReturn(true);
        Region region = new Region();
        region.setLevel("city");
        when(regionService.getById("110100")).thenReturn(region);

        boolean result = storeService.audit("store-1", dto);

        Assertions.assertTrue(result);
        Assertions.assertEquals(StoreAuditStatusEnum.APPROVED.name(), store.getAuditStatus());
        Assertions.assertEquals(StoreStatusEnum.OPEN.value(), store.getStoreDisable());
        Assertions.assertEquals("CITY", store.getAgentLevel());
        Assertions.assertEquals("110100", store.getAgentRegionId());
        Assertions.assertEquals("北京市", store.getAgentRegionName());
        Assertions.assertTrue(member.getHaveStore());
        Assertions.assertEquals("store-1", member.getStoreId());
        verify(memberService).updateById(member);
        verify(clerkService).saveClerk(any(ClerkAddDTO.class));
        verify(storeDetailService).saveOrUpdate(any(StoreDetail.class));
        verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
    }

    @Test
    void auditLegacyEndpointShouldSetRejectedStatus() {
        Store store = new Store();
        store.setId("store-1");
        store.setMemberId("member-1");
        store.setAuditStatus(StoreAuditStatusEnum.SUBMITTED.name());

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).updateById(store);
        when(storeAuditLogService.save(any())).thenReturn(true);

        boolean result = storeService.audit("store-1", 1);

        Assertions.assertTrue(result);
        Assertions.assertEquals(StoreAuditStatusEnum.REJECTED.name(), store.getAuditStatus());
        Assertions.assertEquals(StoreStatusEnum.REFUSED.value(), store.getStoreDisable());
    }

    @Test
    void auditShouldFreezeStore() {
        Store store = new Store();
        store.setId("store-1");
        store.setMemberId("member-1");
        store.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());

        StoreAuditDTO dto = new StoreAuditDTO();
        dto.setAuditStatus(StoreAuditStatusEnum.FROZEN.name());
        dto.setAuditRemark("frozen");

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).updateById(store);
        when(storeAuditLogService.save(any())).thenReturn(true);

        boolean result = storeService.audit("store-1", dto);

        Assertions.assertTrue(result);
        Assertions.assertEquals(StoreAuditStatusEnum.FROZEN.name(), store.getAuditStatus());
        Assertions.assertEquals(StoreStatusEnum.CLOSED.value(), store.getStoreDisable());
    }

    @Test
    void selectApplyTypeShouldCreateDraftForCurrentMember() {
        Member member = new Member();
        member.setId("member-1");
        member.setUsername("member-name");

        StoreApplyTypeSelectDTO dto = new StoreApplyTypeSelectDTO();
        dto.setSubjectType("PERSONAL");

        doReturn(null).when(storeService).getOne(any(), eq(false));
        doAnswer(invocation -> {
            Store saved = invocation.getArgument(0);
            saved.setId("store-1");
            return true;
        }).when(storeService).save(any(Store.class));
        doReturn(true).when(storeService).updateById(any(Store.class));
        when(memberService.getById("member-1")).thenReturn(member);
        when(storeDetailService.getStoreDetail("store-1")).thenReturn(null);
        when(storeDetailService.saveOrUpdate(any(StoreDetail.class))).thenReturn(true);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser)
                    .thenReturn(AuthUser.builder()
                            .id("member-1")
                            .username("member-name")
                            .role(cn.lili.common.security.enums.UserEnums.MEMBER)
                            .identityCode(cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum.CONSUMER)
                            .build());

            boolean result = storeService.selectApplyType(dto, null);

            Assertions.assertTrue(result);
            verify(storeDetailService).saveOrUpdate(any(StoreDetail.class));
            verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
        }
    }

    @Test
    void selectSupplierApplyTypeShouldCreateSupplierDraft() {
        Member member = new Member();
        member.setId("member-1");
        member.setUsername("member-name");

        StoreApplyTypeSelectDTO dto = new StoreApplyTypeSelectDTO();
        dto.setSubjectType("PERSONAL");

        doReturn(null).when(storeService).getOne(any(), eq(false));
        doAnswer(invocation -> {
            Store saved = invocation.getArgument(0);
            saved.setId("store-1");
            return true;
        }).when(storeService).save(any(Store.class));
        doAnswer(invocation -> {
            Store updated = invocation.getArgument(0);
            Assertions.assertEquals(StoreBizTypeEnum.SUPPLIER.name(), updated.getStoreType());
            return true;
        }).when(storeService).updateById(any(Store.class));
        when(memberService.getById("member-1")).thenReturn(member);
        when(storeDetailService.getStoreDetail("store-1")).thenReturn(null);
        when(storeDetailService.saveOrUpdate(any(StoreDetail.class))).thenReturn(true);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser)
                    .thenReturn(AuthUser.builder()
                            .id("member-1")
                            .username("member-name")
                            .role(cn.lili.common.security.enums.UserEnums.MEMBER)
                            .identityCode(cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum.CONSUMER)
                            .build());

            boolean result = storeService.selectSupplierApplyType(dto, null);

            Assertions.assertTrue(result);
            verify(storeDetailService).saveOrUpdate(any(StoreDetail.class));
            verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
        }
    }

    @Test
    void applyPersonalShouldRejectWhenSmsCodeInvalid() {
        StorePersonalApplyDTO dto = new StorePersonalApplyDTO();
        dto.setStoreName("个人店铺");
        dto.setRealName("张三");
        dto.setIdCardNo("110101199001010011");
        dto.setMobile("13800138000");
        dto.setSmsCode("123456");
        dto.setAgreementAccepted(true);

        when(smsUtil.verifyCode("13800138000", cn.lili.modules.verification.entity.enums.VerificationEnums.STORE_APPLY, "uuid-1", "123456"))
                .thenReturn(false);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> storeService.applyPersonal(dto, "uuid-1", null));

        Assertions.assertEquals(ResultCode.VERIFICATION_SMS_CHECKED_ERROR, exception.getResultCode());
    }

    @Test
    void selectApplyTypeShouldRejectWhenCurrentRoleIsNotMember() {
        StoreApplyTypeSelectDTO dto = new StoreApplyTypeSelectDTO();
        dto.setSubjectType("PERSONAL");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser)
                    .thenReturn(AuthUser.builder()
                            .id("member-1")
                            .username("member-name")
                            .role(cn.lili.common.security.enums.UserEnums.STORE)
                            .build());

            ServiceException exception = Assertions.assertThrows(ServiceException.class,
                    () -> storeService.selectApplyType(dto, null));

            Assertions.assertEquals(ResultCode.IDENTITY_NOT_SUPPORTED, exception.getResultCode());
        }
    }

    @Test
    void applyPersonalShouldSubmitApplicationAfterSmsValidation() {
        StorePersonalApplyDTO dto = new StorePersonalApplyDTO();
        dto.setStoreName("个人店铺");
        dto.setRealName("张三");
        dto.setIdCardNo("110101199001010011");
        dto.setMobile("13800138000");
        dto.setSmsCode("123456");
        dto.setAgreementAccepted(true);

        Store draftStore = new Store();
        draftStore.setId("store-1");
        draftStore.setMemberId("member-1");
        draftStore.setStoreDisable(StoreStatusEnum.APPLY.value());
        draftStore.setAuditStatus(StoreAuditStatusEnum.DRAFT.name());

        when(smsUtil.verifyCode("13800138000", cn.lili.modules.verification.entity.enums.VerificationEnums.STORE_APPLY, "uuid-1", "123456"))
                .thenReturn(true);
        doReturn(draftStore).when(storeService).getOne(any(), eq(false));
        when(storeDetailService.getStoreDetail("store-1")).thenReturn(null);
        when(storeDetailService.saveOrUpdate(any(StoreDetail.class))).thenReturn(true);
        doReturn(true).when(storeService).updateById(draftStore);
        doReturn(null).when(storeService).getOne(any());

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser)
                    .thenReturn(AuthUser.builder()
                            .id("member-1")
                            .username("member-name")
                            .role(cn.lili.common.security.enums.UserEnums.MEMBER)
                            .identityCode(cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum.CONSUMER)
                            .build());

            boolean result = storeService.applyPersonal(dto, "uuid-1", null);

            Assertions.assertTrue(result);
            Assertions.assertEquals(StoreAuditStatusEnum.SUBMITTED.name(), draftStore.getAuditStatus());
            Assertions.assertEquals(StoreStatusEnum.APPLYING.name(), draftStore.getStoreDisable());
            Assertions.assertEquals("PERSONAL", draftStore.getSubjectType());
            Assertions.assertEquals(StoreBizTypeEnum.AGENT.name(), draftStore.getStoreType());
            verify(storeDetailService).saveOrUpdate(any(StoreDetail.class));
            verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
        }
    }

    @Test
    void applySupplierPersonalShouldSubmitSupplierApplication() {
        StorePersonalApplyDTO dto = new StorePersonalApplyDTO();
        dto.setStoreName("supplier-store");
        dto.setRealName("person");
        dto.setIdCardNo("110101199001010011");
        dto.setMobile("13800138000");
        dto.setSmsCode("123456");
        dto.setAgreementAccepted(true);

        Store draftStore = new Store();
        draftStore.setId("store-1");
        draftStore.setMemberId("member-1");
        draftStore.setStoreDisable(StoreStatusEnum.APPLY.value());
        draftStore.setAuditStatus(StoreAuditStatusEnum.DRAFT.name());

        when(smsUtil.verifyCode("13800138000", cn.lili.modules.verification.entity.enums.VerificationEnums.STORE_APPLY, "uuid-1", "123456"))
                .thenReturn(true);
        doReturn(draftStore).when(storeService).getOne(any(), eq(false));
        when(storeDetailService.getStoreDetail("store-1")).thenReturn(null);
        when(storeDetailService.saveOrUpdate(any(StoreDetail.class))).thenReturn(true);
        doReturn(true).when(storeService).updateById(draftStore);
        doReturn(null).when(storeService).getOne(any());

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser)
                    .thenReturn(AuthUser.builder()
                            .id("member-1")
                            .username("member-name")
                            .role(cn.lili.common.security.enums.UserEnums.MEMBER)
                            .identityCode(cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum.CONSUMER)
                            .build());

            boolean result = storeService.applySupplierPersonal(dto, "uuid-1", null);

            Assertions.assertTrue(result);
            Assertions.assertEquals(StoreAuditStatusEnum.SUBMITTED.name(), draftStore.getAuditStatus());
            Assertions.assertEquals(StoreStatusEnum.APPLYING.name(), draftStore.getStoreDisable());
            Assertions.assertEquals(StoreBizTypeEnum.SUPPLIER.name(), draftStore.getStoreType());
            Assertions.assertNull(draftStore.getAgentLevel());
            verify(storeDetailService).saveOrUpdate(any(StoreDetail.class));
            verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
        }
    }

    @Test
    void disableShouldEvictStoreCacheAfterUpdate() {
        Store store = new Store();
        store.setId("store-1");

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).update(any(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class));
        when(clerkService.list(any(Wrapper.class))).thenReturn(java.util.Collections.emptyList());

        boolean result = storeService.disable("store-1");

        Assertions.assertTrue(result);
        verify(goodsService).underStoreGoods("store-1");
        verify(cache).remove(CachePrefix.STORE.getPrefix() + "store-1");
    }

    @Test
    void enableShouldEvictStoreCacheAfterUpdate() {
        Store store = new Store();
        store.setId("store-1");

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).updateById(store);

        boolean result = storeService.enable("store-1");

        Assertions.assertTrue(result);
        Assertions.assertEquals(StoreStatusEnum.OPEN.value(), store.getStoreDisable());
        verify(cache).remove(CachePrefix.STORE.getPrefix() + "store-1");
    }
}
