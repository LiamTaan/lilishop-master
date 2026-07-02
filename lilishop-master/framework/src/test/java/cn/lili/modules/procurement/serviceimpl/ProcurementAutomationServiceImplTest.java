package cn.lili.modules.procurement.serviceimpl;

import cn.lili.common.utils.SnowFlake;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuStockDTO;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.vos.GoodsSkuVO;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.goods.service.GoodsGalleryService;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dos.OrderItem;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderTypeEnum;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.order.order.service.OrderItemService;
import cn.lili.modules.order.trade.entity.enums.AfterSaleStatusEnum;
import cn.lili.modules.order.trade.entity.enums.AfterSaleTypeEnum;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.dos.ProcurementOrderItem;
import cn.lili.modules.procurement.entity.enums.ProcurementStatusEnum;
import cn.lili.modules.procurement.service.ProcurementInboundItemService;
import cn.lili.modules.procurement.service.ProcurementInboundService;
import cn.lili.modules.procurement.service.ProcurementOrderItemService;
import cn.lili.modules.procurement.service.ProcurementOrderService;
import cn.lili.modules.search.service.EsGoodsIndexService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementAutomationServiceImplTest {

    @InjectMocks
    private ProcurementAutomationServiceImpl service;

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private ProcurementOrderService procurementOrderService;
    @Mock
    private ProcurementOrderItemService procurementOrderItemService;
    @Mock
    private ProcurementInboundService procurementInboundService;
    @Mock
    private ProcurementInboundItemService procurementInboundItemService;
    @Mock
    private StoreService storeService;
    @Mock
    private AgentRoleRelationService agentRoleRelationService;
    @Mock
    private GoodsService goodsService;
    @Mock
    private GoodsSkuService goodsSkuService;
    @Mock
    private GoodsGalleryService goodsGalleryService;
    @Mock
    private EsGoodsIndexService esGoodsIndexService;

    @BeforeEach
    void setUp() {
        SnowFlake.initialize(1, 1);
    }

    @Test
    void createProcurementByPaidOrderShouldPersistAgentProcurementOrder() {
        Order order = buildSupplierOrder("order-1", OrderStatusEnum.PAID.name());
        OrderItem orderItem = buildOrderItem("item-1", "goods-1", "sku-1", 3, 12D, 36D);
        Store supplierStore = buildStore("supplier-store", StoreBizTypeEnum.SUPPLIER.name(), "Supplier");
        Store agentStore = buildStore("agent-store", StoreBizTypeEnum.AGENT.name(), "Agent");

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(storeService.getById("supplier-store")).thenReturn(supplierStore);
        when(agentRoleRelationService.isAgent("member-1")).thenReturn(true);
        when(storeService.getStoreByMemberId("member-1")).thenReturn(agentStore);
        when(procurementOrderService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(null);
        when(orderItemService.getByOrderSn("order-1")).thenReturn(List.of(orderItem));

        service.createProcurementByPaidOrder("order-1");

        ArgumentCaptor<ProcurementOrder> orderCaptor = ArgumentCaptor.forClass(ProcurementOrder.class);
        verify(procurementOrderService).save(orderCaptor.capture());
        ProcurementOrder savedOrder = orderCaptor.getValue();
        Assertions.assertEquals("order-1", savedOrder.getSourceOrderSn());
        Assertions.assertEquals("agent-store", savedOrder.getStoreId());
        Assertions.assertEquals("supplier-store", savedOrder.getSupplierStoreId());
        Assertions.assertEquals(ProcurementStatusEnum.PENDING_INBOUND.name(), savedOrder.getStatus());

        ArgumentCaptor<List<ProcurementOrderItem>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(procurementOrderItemService).saveBatch(itemCaptor.capture());
        Assertions.assertEquals(1, itemCaptor.getValue().size());
        Assertions.assertEquals("item-1", itemCaptor.getValue().getFirst().getSourceOrderItemSn());
    }

    @Test
    void inboundProcurementByCompletedOrderShouldCreateInboundAndStock() {
        Order order = buildSupplierOrder("order-2", OrderStatusEnum.COMPLETED.name());
        order.setCompleteTime(new Date());
        OrderItem orderItem = buildOrderItem("item-2", "goods-2", "sku-2", 2, 20D, 40D);
        Store supplierStore = buildStore("supplier-store", StoreBizTypeEnum.SUPPLIER.name(), "Supplier");
        Store agentStore = buildStore("agent-store", StoreBizTypeEnum.AGENT.name(), "Agent");

        ProcurementOrder procurementOrder = new ProcurementOrder();
        procurementOrder.setId("po-1");
        procurementOrder.setSourceOrderSn("order-2");
        procurementOrder.setSupplierStoreId("supplier-store");
        procurementOrder.setSupplierStoreName("Supplier");
        procurementOrder.setStatus(ProcurementStatusEnum.PENDING_INBOUND.name());

        ProcurementOrderItem procurementItem = new ProcurementOrderItem();
        procurementItem.setId("poi-1");
        procurementItem.setProcurementOrderId("po-1");
        procurementItem.setGoodsId("goods-2");
        procurementItem.setSkuId("sku-2");
        procurementItem.setGoodsName("Supplier goods");
        procurementItem.setQuantity(2);
        procurementItem.setUnitPriceWithTax(20D);
        procurementItem.setSubtotalWithTax(40D);
        procurementItem.setSourceOrderItemSn("item-2");

        GoodsVO supplierGoods = new GoodsVO();
        supplierGoods.setId("goods-2");
        supplierGoods.setGoodsName("Supplier goods");
        supplierGoods.setPrice(35D);
        supplierGoods.setBarcode("goods-barcode");
        supplierGoods.setSalesModel(GoodsSalesModeEnum.WHOLESALE.name());
        supplierGoods.setGoodsType("PHYSICAL_GOODS");
        supplierGoods.setStoreCategoryPath("sc-1");
        supplierGoods.setTemplateId("tpl-1");
        supplierGoods.setGoodsGalleryList(List.of("https://example.com/g.png"));

        GoodsSkuVO supplierSku = new GoodsSkuVO();
        supplierSku.setId("sku-2");
        supplierSku.setGoodsId("goods-2");
        supplierSku.setGoodsName("Supplier goods");
        supplierSku.setPrice(35D);
        supplierSku.setCost(15D);
        supplierSku.setSn("SUP-SKU-2");
        supplierSku.setBarcode("sku-barcode");
        supplierGoods.setSkuList(List.of(supplierSku));

        GoodsSku targetSku = new GoodsSku();
        targetSku.setId("agent-sku-1");
        targetSku.setGoodsId("agent-goods-1");
        targetSku.setGoodsName("Supplier goods");
        targetSku.setPrice(35D);

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(storeService.getById("supplier-store")).thenReturn(supplierStore);
        when(agentRoleRelationService.isAgent("member-1")).thenReturn(true);
        when(storeService.getStoreByMemberId("member-1")).thenReturn(agentStore);
        when(procurementOrderService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(procurementOrder);
        when(procurementInboundService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(procurementOrderItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(procurementItem));
        when(orderItemService.getByOrderSn("order-2")).thenReturn(List.of(orderItem));
        when(goodsService.getGoodsVO("goods-2")).thenReturn(supplierGoods);
        when(goodsSkuService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(null, targetSku);
        when(goodsService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(null);

        service.inboundProcurementByCompletedOrder("order-2");

        verify(goodsService).save(any(Goods.class));
        verify(goodsGalleryService).add(eq(List.of("https://example.com/g.png")), any(String.class));
        verify(goodsSkuService).saveBatch(anyList());
        verify(procurementInboundService).save(any(ProcurementInbound.class));
        verify(procurementInboundItemService).saveBatch(anyList());

        ArgumentCaptor<List<GoodsSkuStockDTO>> stockCaptor = ArgumentCaptor.forClass(List.class);
        verify(goodsSkuService).updateStocksByType(stockCaptor.capture());
        GoodsSkuStockDTO stockDTO = stockCaptor.getValue().getFirst();
        Assertions.assertEquals("agent-sku-1", stockDTO.getSkuId());
        Assertions.assertEquals(2, stockDTO.getQuantity());

        verify(procurementOrderService).updateById(argThat(item ->
                ProcurementStatusEnum.COMPLETED.name().equals(item.getStatus())));
    }

    @Test
    void closeProcurementByCancelledOrderShouldClosePendingProcurementOrder() {
        ProcurementOrder procurementOrder = new ProcurementOrder();
        procurementOrder.setId("po-close-1");
        procurementOrder.setSourceOrderSn("order-close-1");
        procurementOrder.setStatus(ProcurementStatusEnum.PENDING_INBOUND.name());
        procurementOrder.setRemark("origin");

        when(procurementOrderService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(procurementOrder);

        service.closeProcurementByCancelledOrder("order-close-1", "order cancelled");

        verify(procurementOrderService).updateById(argThat(item ->
                ProcurementStatusEnum.CLOSED.name().equals(item.getStatus())
                        && item.getRemark().contains("origin")
                        && item.getRemark().contains("order cancelled")));
    }

    @Test
    void closeProcurementByCancelledOrderShouldNotCloseCompletedProcurementOrder() {
        ProcurementOrder procurementOrder = new ProcurementOrder();
        procurementOrder.setId("po-close-2");
        procurementOrder.setSourceOrderSn("order-close-2");
        procurementOrder.setStatus(ProcurementStatusEnum.COMPLETED.name());

        when(procurementOrderService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(procurementOrder);

        service.closeProcurementByCancelledOrder("order-close-2", "order cancelled");

        verify(procurementOrderService, never()).updateById(any());
    }

    @Test
    void applyCompletedAfterSaleShouldRestoreAgentStockForReturnGoods() {
        Order order = buildSupplierOrder("order-after-sale-1", OrderStatusEnum.COMPLETED.name());
        Store supplierStore = buildStore("supplier-store", StoreBizTypeEnum.SUPPLIER.name(), "Supplier");
        Store agentStore = buildStore("agent-store", StoreBizTypeEnum.AGENT.name(), "Agent");
        GoodsSku targetSku = new GoodsSku();
        targetSku.setId("agent-sku-after-sale-1");
        targetSku.setGoodsId("agent-goods-after-sale-1");

        AfterSale afterSale = new AfterSale();
        afterSale.setOrderSn("order-after-sale-1");
        afterSale.setSkuId("sku-after-sale-1");
        afterSale.setNum(2);
        afterSale.setServiceStatus(AfterSaleStatusEnum.COMPLETE.name());
        afterSale.setServiceType(AfterSaleTypeEnum.RETURN_GOODS.name());

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(storeService.getById("supplier-store")).thenReturn(supplierStore);
        when(agentRoleRelationService.isAgent("member-1")).thenReturn(true);
        when(storeService.getStoreByMemberId("member-1")).thenReturn(agentStore);
        when(goodsSkuService.getOne(any(LambdaQueryWrapper.class), eq(false))).thenReturn(targetSku);

        service.applyCompletedAfterSale(afterSale);

        ArgumentCaptor<List<GoodsSkuStockDTO>> stockCaptor = ArgumentCaptor.forClass(List.class);
        verify(goodsSkuService).updateStocksByType(stockCaptor.capture());
        GoodsSkuStockDTO stockDTO = stockCaptor.getValue().getFirst();
        Assertions.assertEquals("agent-sku-after-sale-1", stockDTO.getSkuId());
        Assertions.assertEquals(2, stockDTO.getQuantity());
    }

    @Test
    void applyCompletedAfterSaleShouldIgnoreReturnMoney() {
        AfterSale afterSale = new AfterSale();
        afterSale.setOrderSn("order-after-sale-2");
        afterSale.setSkuId("sku-after-sale-2");
        afterSale.setNum(2);
        afterSale.setServiceStatus(AfterSaleStatusEnum.COMPLETE.name());
        afterSale.setServiceType(AfterSaleTypeEnum.RETURN_MONEY.name());

        service.applyCompletedAfterSale(afterSale);

        verify(goodsSkuService, never()).updateStocksByType(anyList());
        verify(orderMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    private Order buildSupplierOrder(String orderSn, String status) {
        Order order = new Order();
        order.setSn(orderSn);
        order.setStoreId("supplier-store");
        order.setStoreName("Supplier");
        order.setMemberId("member-1");
        order.setMemberName("Agent Member");
        order.setOrderType(OrderTypeEnum.NORMAL.name());
        order.setOrderStatus(status);
        order.setPaymentTime(new Date());
        return order;
    }

    private OrderItem buildOrderItem(String sn, String goodsId, String skuId, int num, double unitPrice, double subTotal) {
        OrderItem item = new OrderItem();
        item.setSn(sn);
        item.setGoodsId(goodsId);
        item.setSkuId(skuId);
        item.setGoodsName("Supplier goods");
        item.setNum(num);
        item.setUnitPrice(unitPrice);
        item.setSubTotal(subTotal);
        item.setGoodsPrice(unitPrice);
        return item;
    }

    private Store buildStore(String id, String storeType, String storeName) {
        Store store = new Store();
        store.setId(id);
        store.setStoreType(storeType);
        store.setStoreName(storeName);
        store.setSelfOperated(false);
        return store;
    }
}
