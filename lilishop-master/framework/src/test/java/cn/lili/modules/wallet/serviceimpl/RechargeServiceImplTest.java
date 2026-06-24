package cn.lili.modules.wallet.serviceimpl;

import cn.lili.modules.order.order.entity.enums.PayStatusEnum;
import cn.lili.modules.wallet.entity.dos.Recharge;
import cn.lili.modules.wallet.entity.dto.MemberWalletUpdateDTO;
import cn.lili.modules.wallet.service.MemberWalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RechargeServiceImplTest {

    @Spy
    @InjectMocks
    private RechargeServiceImpl rechargeService;

    @Mock
    private MemberWalletService memberWalletService;

    @Test
    void paySuccessShouldUpdateRechargeAndIncreaseWalletOnce() {
        Recharge recharge = new Recharge("R20260619001", "member-1", "buyer-1", 6D);
        recharge.setPayStatus(PayStatusEnum.UNPAID.name());

        doReturn(recharge).when(rechargeService).getOne(any());
        doReturn(true).when(rechargeService).updateById(recharge);

        rechargeService.paySuccess("R20260619001", "third-party-001", "ALIPAY");

        Assertions.assertEquals(PayStatusEnum.PAID.name(), recharge.getPayStatus());
        Assertions.assertEquals("third-party-001", recharge.getReceivableNo());
        Assertions.assertEquals("ALIPAY", recharge.getRechargeWay());
        Assertions.assertNotNull(recharge.getPayTime());

        verify(memberWalletService).increase(any(MemberWalletUpdateDTO.class));
    }

    @Test
    void paySuccessShouldIgnoreAlreadyPaidRecharge() {
        Recharge recharge = new Recharge("R20260619002", "member-2", "buyer-2", 8D);
        recharge.setPayStatus(PayStatusEnum.PAID.name());

        doReturn(recharge).when(rechargeService).getOne(any());

        rechargeService.paySuccess("R20260619002", "third-party-002", "WECHAT");

        Assertions.assertEquals(PayStatusEnum.PAID.name(), recharge.getPayStatus());
        Assertions.assertNull(recharge.getReceivableNo());
        Assertions.assertNull(recharge.getRechargeWay());

        verify(rechargeService, never()).updateById(any(Recharge.class));
        verify(memberWalletService, never()).increase(any(MemberWalletUpdateDTO.class));
    }
}
