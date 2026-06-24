package cn.lili.modules.order.order.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.SnowFlake;
import cn.lili.modules.distribution.service.DistributionOrderService;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.order.entity.dos.StoreFlow;
import cn.lili.modules.order.order.entity.dto.StoreFlowProfitSharingDTO;
import cn.lili.modules.order.order.service.OrderItemService;
import cn.lili.modules.order.order.service.OrderService;
import cn.lili.modules.payment.service.RefundLogService;
import cn.lili.modules.store.service.BillService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class StoreFlowServiceImplTest {

    @Spy
    @InjectMocks
    private StoreFlowServiceImpl storeFlowService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private RefundLogService refundLogService;

    @Mock
    private BillService billService;

    @Mock
    private DistributionOrderService distributionOrderService;

    @Test
    void refundOrderShouldThrowBusinessExceptionWhenRefundLogMissing() {
        AfterSale afterSale = new AfterSale();
        afterSale.setSn("A-refund-3");
        afterSale.setOrderSn("O-refund-3");
        afterSale.setOrderItemSn("OI-refund-3");
        afterSale.setStoreId("store-1");
        afterSale.setStoreName("Store 1");
        afterSale.setMemberId("member-1");
        afterSale.setMemberName("Member 1");
        afterSale.setGoodsId("goods-1");
        afterSale.setGoodsName("Goods 1");
        afterSale.setSkuId("sku-1");
        afterSale.setGoodsImage("image");
        afterSale.setSpecs("specs");
        afterSale.setNum(1);
        afterSale.setActualRefundPrice(0.01D);

        StoreFlow payStoreFlow = new StoreFlow();
        payStoreFlow.setOrderItemSn("OI-refund-3");
        payStoreFlow.setCategoryId("cat-1");
        payStoreFlow.setCommissionPrice(0D);
        payStoreFlow.setNum(1);
        payStoreFlow.setDistributionRebate(0D);
        payStoreFlow.setFinalPrice(0.01D);
        payStoreFlow.setSiteCouponCommission(0D);
        payStoreFlow.setSiteCouponPrice(0D);
        payStoreFlow.setSiteCouponPoint(0D);
        payStoreFlow.setProfitSharing(JSONUtil.toJsonStr(new StoreFlowProfitSharingDTO()));

        doReturn(payStoreFlow).when(storeFlowService).getOne(any());

        ServiceException exception;
        try (MockedStatic<SnowFlake> mockedSnowFlake = mockStatic(SnowFlake.class)) {
            mockedSnowFlake.when(() -> SnowFlake.createStr("SF")).thenReturn("SF-test-1");

            exception = Assertions.assertThrows(ServiceException.class,
                    () -> storeFlowService.refundOrder(afterSale));
        }

        Assertions.assertEquals(ResultCode.PAY_ERROR, exception.getResultCode());
    }
}
