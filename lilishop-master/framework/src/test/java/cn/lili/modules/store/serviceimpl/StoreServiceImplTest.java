package cn.lili.modules.store.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dos.Clerk;
import cn.lili.modules.member.entity.dto.ClerkAddDTO;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.dos.StoreDetail;
import cn.lili.modules.store.entity.dto.StoreAuditDTO;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreAuditLogService;
import cn.lili.modules.store.service.StoreDetailService;
import cn.lili.modules.goods.service.GoodsService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
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
    private Cache cache;

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

        Member member = new Member();
        member.setId("member-1");

        StoreAuditDTO dto = new StoreAuditDTO();
        dto.setAuditStatus(StoreAuditStatusEnum.APPROVED.name());
        dto.setAuditRemark("approved");

        doReturn(store).when(storeService).getById("store-1");
        doReturn(true).when(storeService).updateById(store);
        when(memberService.getById("member-1")).thenReturn(member);
        when(memberService.updateById(member)).thenReturn(true);
        when(clerkService.saveClerk(any(ClerkAddDTO.class))).thenReturn(new Clerk());
        when(storeAuditLogService.save(any())).thenReturn(true);

        boolean result = storeService.audit("store-1", dto);

        Assertions.assertTrue(result);
        Assertions.assertEquals(StoreAuditStatusEnum.APPROVED.name(), store.getAuditStatus());
        Assertions.assertEquals(StoreStatusEnum.OPEN.value(), store.getStoreDisable());
        Assertions.assertTrue(member.getHaveStore());
        Assertions.assertEquals("store-1", member.getStoreId());
        verify(memberService).updateById(member);
        verify(clerkService).saveClerk(any(ClerkAddDTO.class));
        verify(storeDetailService).update(any());
        verify(cache).remove(eq(CachePrefix.STORE.getPrefix() + "store-1"));
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
    void applySecondStepShouldRejectWhenCurrentMemberHasNoStoreDraft() {
        doReturn(null).when(storeService).getOne(any(), eq(false));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> storeService.applySecondStep(new cn.lili.modules.store.entity.dto.StoreBankDTO()));

        Assertions.assertEquals(ResultCode.STORE_NOT_EXIST, exception.getResultCode());
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
