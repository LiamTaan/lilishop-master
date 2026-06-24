package cn.lili.modules.payment.kit.plugin.bank;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.payment.entity.RefundLog;
import cn.lili.modules.payment.service.PaymentService;
import cn.lili.modules.payment.service.RefundLogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransferPluginTest {

    @InjectMocks
    private BankTransferPlugin bankTransferPlugin;

    @Mock
    private RefundLogService refundLogService;

    @Mock
    private PaymentService paymentService;

    @Test
    void refundShouldPersistOfflineRefundAsSuccessful() {
        RefundLog refundLog = RefundLog.builder()
                .afterSaleNo("A-refund-1")
                .orderSn("O-refund-1")
                .outOrderNo("AF-REFUND-1")
                .build();

        bankTransferPlugin.refund(refundLog);

        Assertions.assertTrue(refundLog.getIsRefund());
        Assertions.assertEquals("AF-REFUND-1", refundLog.getReceivableNo());
        verify(refundLogService).save(refundLog);
    }

    @Test
    void refundShouldThrowWhenRefundLogSaveFails() {
        RefundLog refundLog = RefundLog.builder()
                .afterSaleNo("A-refund-2")
                .orderSn("O-refund-2")
                .outOrderNo("AF-REFUND-2")
                .build();
        when(refundLogService.save(any(RefundLog.class))).thenThrow(new RuntimeException("db failure"));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> bankTransferPlugin.refund(refundLog));

        Assertions.assertEquals(ResultCode.PAY_ERROR, exception.getResultCode());
    }
}
