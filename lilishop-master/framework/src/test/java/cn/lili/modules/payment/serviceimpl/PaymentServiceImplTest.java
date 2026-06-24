package cn.lili.modules.payment.serviceimpl;

import cn.lili.modules.payment.kit.CashierSupport;
import cn.lili.modules.payment.kit.dto.PayParam;
import cn.lili.modules.payment.kit.dto.PaymentSuccessParams;
import cn.lili.modules.payment.kit.params.CashierExecute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private CashierSupport cashierSupport;

    @Mock
    private CashierExecute cashierExecute;

    @Test
    void successShouldSkipDuplicatePaymentCallback() {
        PayParam payParam = new PayParam();
        payParam.setOrderType("RECHARGE");
        payParam.setSn("Y20260619001");
        payParam.setClientType("PC");
        PaymentSuccessParams paymentSuccessParams = new PaymentSuccessParams("ALIPAY", "flow-001", 6D, payParam);

        ReflectionTestUtils.setField(paymentService, "cashierExecutes", List.of(cashierExecute));
        when(cashierSupport.paymentResult(payParam)).thenReturn(true);

        paymentService.success(paymentSuccessParams);

        verify(cashierExecute, never()).paymentSuccess(paymentSuccessParams);
    }

    @Test
    void successShouldDispatchUnpaidPaymentCallback() {
        PayParam payParam = new PayParam();
        payParam.setOrderType("RECHARGE");
        payParam.setSn("Y20260619002");
        payParam.setClientType("PC");
        PaymentSuccessParams paymentSuccessParams = new PaymentSuccessParams("WECHAT", "flow-002", 8D, payParam);

        ReflectionTestUtils.setField(paymentService, "cashierExecutes", List.of(cashierExecute));
        when(cashierSupport.paymentResult(payParam)).thenReturn(false);

        paymentService.success(paymentSuccessParams);

        verify(cashierExecute).paymentSuccess(paymentSuccessParams);
    }
}
