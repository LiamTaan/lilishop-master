package cn.lili.modules.order.order.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.order.order.entity.dos.Receipt;
import cn.lili.modules.order.order.entity.dto.ReceiptInvoicingDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

    @Spy
    @InjectMocks
    private ReceiptServiceImpl receiptService;

    @Mock
    private AgentRoleRelationService agentRoleRelationService;

    @Mock
    private AgentStoreBindService agentStoreBindService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDetailShouldRejectAccessToOtherMemberReceipt() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-2");
        receipt.setStoreId("store-1");

        AuthUser currentUser = AuthUser.builder()
                .id("member-1")
                .role(UserEnums.MEMBER)
                .build();

        doReturn(receipt).when(receiptService).getById("receipt-1");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            ServiceException exception = Assertions.assertThrows(ServiceException.class,
                    () -> receiptService.getDetail("receipt-1"));

            Assertions.assertEquals(ResultCode.USER_AUTHORITY_ERROR, exception.getResultCode());
        }
    }

    @Test
    void getDetailShouldRejectAgentAccessOutsidePermittedStore() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-1");
        receipt.setStoreId("store-2");

        AuthUser currentUser = AuthUser.builder()
                .id("member-1")
                .role(UserEnums.MEMBER)
                .build();

        doReturn(receipt).when(receiptService).getById("receipt-1");
        when(agentRoleRelationService.isAgent("member-1")).thenReturn(true);
        when(agentStoreBindService.listApprovedStoreIdsByAgentMemberId("member-1")).thenReturn(List.of("store-1"));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            ServiceException exception = Assertions.assertThrows(ServiceException.class,
                    () -> receiptService.getDetail("receipt-1"));

            Assertions.assertEquals(ResultCode.USER_AUTHORITY_ERROR, exception.getResultCode());
        }
    }

    @Test
    void invoicingShouldUpdateReceiptWhenPermissionGranted() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-1");
        receipt.setStoreId("store-1");

        ReceiptInvoicingDTO dto = new ReceiptInvoicingDTO();
        dto.setInvoiceAddress("  https://example.com/invoice.pdf  ");

        AuthUser currentUser = AuthUser.builder()
                .id("member-1")
                .role(UserEnums.MEMBER)
                .build();

        doReturn(receipt).when(receiptService).getById("receipt-1");
        doReturn(true).when(receiptService).saveOrUpdate(receipt);
        when(agentRoleRelationService.isAgent("member-1")).thenReturn(true);
        when(agentStoreBindService.listApprovedStoreIdsByAgentMemberId("member-1")).thenReturn(List.of("store-1"));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            Receipt result = receiptService.invoicing("receipt-1", dto);

            Assertions.assertEquals("https://example.com/invoice.pdf", result.getInvoiceAddress());
            Assertions.assertEquals(1, result.getReceiptStatus());
            verify(receiptService).saveOrUpdate(receipt);
        }
    }

    @Test
    void saveReceiptShouldUseSaveOrUpdateWhenReceiptHasId() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setReceiptTitle("QA Receipt");

        AuthUser currentUser = AuthUser.builder()
                .id("member-1")
                .role(UserEnums.MEMBER)
                .build();

        doReturn(null).when(receiptService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(receiptService).saveOrUpdate(receipt);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            Receipt result = receiptService.saveReceipt(receipt);

            Assertions.assertSame(receipt, result);
            Assertions.assertEquals("member-1", result.getMemberId());
            verify(receiptService).saveOrUpdate(receipt);
        }
    }

    @Test
    void getDetailShouldAllowManagerAccess() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-1");
        receipt.setStoreId("store-1");

        AuthUser currentUser = AuthUser.builder()
                .id("manager-1")
                .role(UserEnums.MANAGER)
                .build();

        doReturn(receipt).when(receiptService).getById("receipt-1");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            Receipt result = receiptService.getDetail("receipt-1");

            Assertions.assertSame(receipt, result);
        }
    }

    @Test
    void invoicingShouldAllowStoreAccessWithinSameStore() {
        Receipt receipt = new Receipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-1");
        receipt.setStoreId("store-1");

        ReceiptInvoicingDTO dto = new ReceiptInvoicingDTO();
        dto.setInvoiceAddress(" https://example.com/store-invoice.pdf ");

        AuthUser currentUser = AuthUser.builder()
                .id("clerk-1")
                .role(UserEnums.STORE)
                .storeId("store-1")
                .build();

        doReturn(receipt).when(receiptService).getById("receipt-1");
        doReturn(true).when(receiptService).saveOrUpdate(receipt);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(currentUser);

            Receipt result = receiptService.invoicing("receipt-1", dto);

            Assertions.assertEquals("https://example.com/store-invoice.pdf", result.getInvoiceAddress());
            Assertions.assertEquals(1, result.getReceiptStatus());
        }
    }
}
