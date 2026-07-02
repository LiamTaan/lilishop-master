package cn.lili.modules.procurement.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.common.utils.SnowFlake;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuStockDTO;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.entity.enums.GoodsStockTypeEnum;
import cn.lili.modules.goods.entity.vos.GoodsSkuVO;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.goods.service.GoodsGalleryService;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dos.OrderItem;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.dos.ProcurementInboundItem;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.dos.ProcurementOrderItem;
import cn.lili.modules.procurement.entity.enums.ProcurementStatusEnum;
import cn.lili.modules.procurement.service.ProcurementAutomationService;
import cn.lili.modules.procurement.service.ProcurementInboundItemService;
import cn.lili.modules.procurement.service.ProcurementInboundService;
import cn.lili.modules.procurement.service.ProcurementOrderItemService;
import cn.lili.modules.procurement.service.ProcurementOrderService;
import cn.lili.modules.search.service.EsGoodsIndexService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import cn.lili.modules.order.trade.entity.enums.AfterSaleStatusEnum;
import cn.lili.modules.order.trade.entity.enums.AfterSaleTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 代理商向供货商采购的自动化处理。
 *
 * @author OpenAI
 */
@Service
public class ProcurementAutomationServiceImpl implements ProcurementAutomationService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private cn.lili.modules.order.order.service.OrderItemService orderItemService;
    @Resource
    private ProcurementOrderService procurementOrderService;
    @Resource
    private ProcurementOrderItemService procurementOrderItemService;
    @Resource
    private ProcurementInboundService procurementInboundService;
    @Resource
    private ProcurementInboundItemService procurementInboundItemService;
    @Resource
    private StoreService storeService;
    @Resource
    private AgentRoleRelationService agentRoleRelationService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private GoodsSkuService goodsSkuService;
    @Resource
    private GoodsGalleryService goodsGalleryService;
    @Resource
    private EsGoodsIndexService esGoodsIndexService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProcurementByPaidOrder(String orderSn) {
        Order order = getOrderBySn(orderSn);
        if (!isEligibleProcurementOrder(order)) {
            return;
        }
        if (findProcurementOrder(orderSn) != null) {
            return;
        }
        Store supplierStore = storeService.getById(order.getStoreId());
        Store agentStore = resolveAgentStore(order.getMemberId());
        if (supplierStore == null || agentStore == null) {
            return;
        }
        List<OrderItem> orderItems = orderItemService.getByOrderSn(orderSn);
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        ProcurementOrder procurementOrder = new ProcurementOrder();
        procurementOrder.setId(SnowFlake.getIdStr());
        procurementOrder.setOrderSn(SnowFlake.createStr("PO"));
        procurementOrder.setStoreId(agentStore.getId());
        procurementOrder.setStoreName(agentStore.getStoreName());
        procurementOrder.setSourceOrderSn(orderSn);
        procurementOrder.setSupplierStoreId(supplierStore.getId());
        procurementOrder.setSupplierStoreName(supplierStore.getStoreName());
        procurementOrder.setMakerId(order.getMemberId());
        procurementOrder.setMakerName(order.getMemberName());
        procurementOrder.setAuditTime(order.getPaymentTime());
        procurementOrder.setRemark("订单支付成功自动生成");
        procurementOrder.setStatus(ProcurementStatusEnum.PENDING_INBOUND.name());

        int totalQuantity = 0;
        double totalAmount = 0D;
        List<ProcurementOrderItem> procurementItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            ProcurementOrderItem procurementItem = new ProcurementOrderItem();
            procurementItem.setId(SnowFlake.getIdStr());
            procurementItem.setProcurementOrderId(procurementOrder.getId());
            procurementItem.setGoodsId(orderItem.getGoodsId());
            procurementItem.setSkuId(orderItem.getSkuId());
            procurementItem.setGoodsName(orderItem.getGoodsName());
            procurementItem.setSourceOrderItemSn(orderItem.getSn());
            procurementItem.setSupplierStoreId(supplierStore.getId());
            procurementItem.setSupplierStoreName(supplierStore.getStoreName());
            procurementItem.setRetailPrice(orderItem.getGoodsPrice());
            procurementItem.setQuantity(orderItem.getNum());
            procurementItem.setTaxRate(0);
            procurementItem.setUnitPriceWithTax(orderItem.getUnitPrice());
            procurementItem.setUnitPriceWithoutTax(orderItem.getUnitPrice());
            procurementItem.setSubtotalWithTax(orderItem.getSubTotal());
            procurementItem.setSubtotalWithoutTax(orderItem.getSubTotal());
            procurementItem.setReceivedQuantity(0);
            procurementItems.add(procurementItem);
            totalQuantity += orderItem.getNum() == null ? 0 : orderItem.getNum();
            totalAmount = CurrencyUtil.add(totalAmount, orderItem.getSubTotal() == null ? 0D : orderItem.getSubTotal());
        }
        procurementOrder.setTotalQuantity(totalQuantity);
        procurementOrder.setTotalAmount(totalAmount);
        procurementOrderService.save(procurementOrder);
        procurementOrderItemService.saveBatch(procurementItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeProcurementByCancelledOrder(String orderSn, String reason) {
        ProcurementOrder procurementOrder = findProcurementOrder(orderSn);
        if (procurementOrder == null) {
            return;
        }
        if (ProcurementStatusEnum.COMPLETED.name().equals(procurementOrder.getStatus())) {
            return;
        }
        procurementOrder.setStatus(ProcurementStatusEnum.CLOSED.name());
        String message = CharSequenceUtil.blankToDefault(reason, "订单取消自动关闭");
        procurementOrder.setRemark(CharSequenceUtil.isBlank(procurementOrder.getRemark())
                ? message
                : procurementOrder.getRemark() + "；" + message);
        procurementOrderService.updateById(procurementOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundProcurementByCompletedOrder(String orderSn) {
        Order order = getOrderBySn(orderSn);
        if (!isEligibleProcurementOrder(order) || !OrderStatusEnum.COMPLETED.name().equals(order.getOrderStatus())) {
            return;
        }
        ProcurementOrder procurementOrder = findProcurementOrder(orderSn);
        if (procurementOrder == null) {
            createProcurementByPaidOrder(orderSn);
            procurementOrder = findProcurementOrder(orderSn);
        }
        if (procurementOrder == null || ProcurementStatusEnum.COMPLETED.name().equals(procurementOrder.getStatus())) {
            return;
        }
        if (procurementInboundService.count(Wrappers.<ProcurementInbound>lambdaQuery()
                .eq(ProcurementInbound::getProcurementOrderId, procurementOrder.getId())) > 0) {
            return;
        }

        Store agentStore = resolveAgentStore(order.getMemberId());
        if (agentStore == null) {
            return;
        }
        List<ProcurementOrderItem> procurementItems = procurementOrderItemService.list(Wrappers.<ProcurementOrderItem>lambdaQuery()
                .eq(ProcurementOrderItem::getProcurementOrderId, procurementOrder.getId()));
        if (procurementItems.isEmpty()) {
            return;
        }
        Map<String, OrderItem> orderItemMap = orderItemService.getByOrderSn(orderSn).stream()
                .collect(Collectors.toMap(OrderItem::getSn, item -> item, (left, right) -> left));

        ProcurementInbound inbound = new ProcurementInbound();
        inbound.setId(SnowFlake.getIdStr());
        inbound.setInboundSn(SnowFlake.createStr("PI"));
        inbound.setStoreId(agentStore.getId());
        inbound.setProcurementOrderId(procurementOrder.getId());
        inbound.setOperatorId(order.getMemberId());
        inbound.setOperatorName(order.getMemberName());
        inbound.setInboundTime(order.getCompleteTime() == null ? new Date() : order.getCompleteTime());

        List<ProcurementInboundItem> inboundItems = new ArrayList<>();
        List<GoodsSkuStockDTO> stockUpdates = new ArrayList<>();
        int totalQuantity = 0;
        double totalCost = 0D;
        double totalRetail = 0D;

        Map<String, GoodsVO> supplierGoodsCache = new HashMap<>();
        for (ProcurementOrderItem procurementItem : procurementItems) {
            OrderItem sourceOrderItem = orderItemMap.get(procurementItem.getSourceOrderItemSn());
            if (sourceOrderItem == null || sourceOrderItem.getNum() == null || sourceOrderItem.getNum() <= 0
                    || CharSequenceUtil.isBlank(procurementItem.getGoodsId())) {
                continue;
            }
            GoodsVO supplierGoods = supplierGoodsCache.computeIfAbsent(procurementItem.getGoodsId(), goodsService::getGoodsVO);
            if (supplierGoods == null) {
                continue;
            }
            GoodsSku targetSku = ensureTargetSku(agentStore, procurementOrder, supplierGoods, procurementItem, sourceOrderItem);
            if (targetSku == null) {
                continue;
            }

            ProcurementInboundItem inboundItem = new ProcurementInboundItem();
            inboundItem.setId(SnowFlake.getIdStr());
            inboundItem.setInboundId(inbound.getId());
            inboundItem.setProcurementOrderItemId(procurementItem.getId());
            inboundItem.setGoodsId(targetSku.getGoodsId());
            inboundItem.setSkuId(targetSku.getId());
            inboundItem.setGoodsName(targetSku.getGoodsName());
            inboundItem.setExpectedQuantity(sourceOrderItem.getNum());
            inboundItem.setInboundQuantity(sourceOrderItem.getNum());
            inboundItems.add(inboundItem);

            GoodsSkuStockDTO stockDTO = new GoodsSkuStockDTO();
            stockDTO.setGoodsId(targetSku.getGoodsId());
            stockDTO.setSkuId(targetSku.getId());
            stockDTO.setQuantity(sourceOrderItem.getNum());
            stockDTO.setType(GoodsStockTypeEnum.ADD.name());
            stockUpdates.add(stockDTO);

            procurementItem.setReceivedQuantity(sourceOrderItem.getNum());
            procurementOrderItemService.updateById(procurementItem);

            totalQuantity += sourceOrderItem.getNum();
            totalCost = CurrencyUtil.add(totalCost, procurementItem.getSubtotalWithTax() == null ? 0D : procurementItem.getSubtotalWithTax());
            totalRetail = CurrencyUtil.add(totalRetail, CurrencyUtil.mul(targetSku.getPrice() == null ? 0D : targetSku.getPrice(), sourceOrderItem.getNum()));
        }
        if (inboundItems.isEmpty()) {
            return;
        }

        inbound.setExpectedQuantity(totalQuantity);
        inbound.setConfirmedQuantity(totalQuantity);
        inbound.setPendingQuantity(0);
        inbound.setTotalCost(totalCost);
        inbound.setTotalRetailAmount(totalRetail);
        procurementInboundService.save(inbound);
        procurementInboundItemService.saveBatch(inboundItems);
        goodsSkuService.updateStocksByType(stockUpdates);

        procurementOrder.setStatus(ProcurementStatusEnum.COMPLETED.name());
        procurementOrder.setRemark("代理商确认收货后自动入库");
        procurementOrderService.updateById(procurementOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCompletedAfterSale(AfterSale afterSale) {
        if (afterSale == null
                || !AfterSaleStatusEnum.COMPLETE.name().equals(afterSale.getServiceStatus())
                || !AfterSaleTypeEnum.RETURN_GOODS.name().equals(afterSale.getServiceType())) {
            return;
        }
        Order order = getOrderBySn(afterSale.getOrderSn());
        if (!isEligibleProcurementOrder(order)) {
            return;
        }
        Store agentStore = resolveAgentStore(order.getMemberId());
        if (agentStore == null) {
            return;
        }
        GoodsSku targetSku = goodsSkuService.getOne(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getStoreId, agentStore.getId())
                .eq(GoodsSku::getSourceStoreId, order.getStoreId())
                .eq(GoodsSku::getSourceSkuId, afterSale.getSkuId())
                .eq(GoodsSku::getDeleteFlag, Boolean.FALSE)
                .last("limit 1"), false);
        if (targetSku == null) {
            return;
        }
        GoodsSkuStockDTO stockDTO = new GoodsSkuStockDTO();
        stockDTO.setGoodsId(targetSku.getGoodsId());
        stockDTO.setSkuId(targetSku.getId());
        stockDTO.setQuantity(afterSale.getNum() == null ? 0 : afterSale.getNum());
        stockDTO.setType(GoodsStockTypeEnum.ADD.name());
        if (stockDTO.getQuantity() > 0) {
            goodsSkuService.updateStocksByType(List.of(stockDTO));
        }
    }

    private ProcurementOrder findProcurementOrder(String orderSn) {
        if (CharSequenceUtil.isBlank(orderSn)) {
            return null;
        }
        return procurementOrderService.getOne(Wrappers.<ProcurementOrder>lambdaQuery()
                .eq(ProcurementOrder::getSourceOrderSn, orderSn)
                .last("limit 1"), false);
    }

    private Order getOrderBySn(String orderSn) {
        if (CharSequenceUtil.isBlank(orderSn)) {
            return null;
        }
        return orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getSn, orderSn)
                .last("limit 1"));
    }

    private boolean isEligibleProcurementOrder(Order order) {
        if (order == null || !Objects.equals(order.getOrderType(), cn.lili.modules.order.order.entity.enums.OrderTypeEnum.NORMAL.name())) {
            return false;
        }
        Store supplierStore = storeService.getById(order.getStoreId());
        if (supplierStore == null || !StoreBizTypeEnum.SUPPLIER.name().equalsIgnoreCase(supplierStore.getStoreType())) {
            return false;
        }
        return resolveAgentStore(order.getMemberId()) != null;
    }

    private Store resolveAgentStore(String memberId) {
        if (CharSequenceUtil.isBlank(memberId) || !agentRoleRelationService.isAgent(memberId)) {
            return null;
        }
        Store agentStore = storeService.getStoreByMemberId(memberId);
        if (agentStore == null || !StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(agentStore.getStoreType())) {
            return null;
        }
        return agentStore;
    }

    private GoodsSku ensureTargetSku(Store agentStore, ProcurementOrder procurementOrder, GoodsVO supplierGoods,
                                     ProcurementOrderItem procurementItem, OrderItem sourceOrderItem) {
        GoodsSku targetSku = goodsSkuService.getOne(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getStoreId, agentStore.getId())
                .eq(GoodsSku::getSourceStoreId, procurementOrder.getSupplierStoreId())
                .eq(GoodsSku::getSourceSkuId, procurementItem.getSkuId())
                .eq(GoodsSku::getDeleteFlag, Boolean.FALSE)
                .last("limit 1"), false);
        if (targetSku != null) {
            targetSku.setCost(sourceOrderItem.getUnitPrice());
            goodsSkuService.updateById(targetSku);
            goodsSkuService.clearCache(targetSku.getId());
            esGoodsIndexService.getResetEsGoodsIndex(targetSku);
            return targetSku;
        }

        Goods targetGoods = goodsService.getOne(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStoreId, agentStore.getId())
                .eq(Goods::getSourceStoreId, procurementOrder.getSupplierStoreId())
                .eq(Goods::getSourceGoodsId, procurementItem.getGoodsId())
                .eq(Goods::getDeleteFlag, Boolean.FALSE)
                .last("limit 1"), false);
        if (targetGoods == null) {
            targetGoods = cloneSupplierGoods(agentStore, procurementOrder, supplierGoods, procurementItem, sourceOrderItem);
        } else {
            cloneMissingSku(agentStore, procurementOrder, targetGoods, supplierGoods, procurementItem, sourceOrderItem);
        }

        return goodsSkuService.getOne(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getStoreId, agentStore.getId())
                .eq(GoodsSku::getSourceStoreId, procurementOrder.getSupplierStoreId())
                .eq(GoodsSku::getSourceSkuId, procurementItem.getSkuId())
                .eq(GoodsSku::getDeleteFlag, Boolean.FALSE)
                .last("limit 1"), false);
    }

    private Goods cloneSupplierGoods(Store agentStore, ProcurementOrder procurementOrder, GoodsVO supplierGoods,
                                     ProcurementOrderItem procurementItem, OrderItem sourceOrderItem) {
        Goods targetGoods = new Goods();
        targetGoods.setId(SnowFlake.getIdStr());
        targetGoods.setGoodsName(supplierGoods.getGoodsName());
        targetGoods.setBarcode(supplierGoods.getBarcode());
        targetGoods.setPrice(supplierGoods.getPrice());
        targetGoods.setBrandId(supplierGoods.getBrandId());
        targetGoods.setCategoryPath(supplierGoods.getCategoryPath());
        targetGoods.setGoodsUnit(supplierGoods.getGoodsUnit());
        targetGoods.setSellingPoint(supplierGoods.getSellingPoint());
        targetGoods.setMarketEnable(GoodsStatusEnum.DOWN.name());
        targetGoods.setIntro(supplierGoods.getIntro());
        targetGoods.setBuyCount(0);
        targetGoods.setQuantity(0);
        targetGoods.setGrade(100D);
        targetGoods.setThumbnail(supplierGoods.getThumbnail());
        targetGoods.setSmall(supplierGoods.getSmall());
        targetGoods.setOriginal(supplierGoods.getOriginal());
        targetGoods.setStoreCategoryPath(supplierGoods.getStoreCategoryPath());
        targetGoods.setCommentNum(0);
        targetGoods.setStoreId(agentStore.getId());
        targetGoods.setStoreName(agentStore.getStoreName());
        targetGoods.setSourceStoreId(procurementOrder.getSupplierStoreId());
        targetGoods.setSourceStoreName(procurementOrder.getSupplierStoreName());
        targetGoods.setSourceGoodsId(supplierGoods.getId());
        targetGoods.setTemplateId(supplierGoods.getTemplateId());
        targetGoods.setAuthFlag(GoodsAuthEnum.PASS.name());
        targetGoods.setAuthMessage(null);
        targetGoods.setUnderMessage(null);
        targetGoods.setSelfOperated(agentStore.getSelfOperated());
        targetGoods.setMobileIntro(supplierGoods.getMobileIntro());
        targetGoods.setGoodsVideo(supplierGoods.getGoodsVideo());
        targetGoods.setRecommend(Boolean.FALSE.equals(supplierGoods.getRecommend()) ? Boolean.FALSE : supplierGoods.getRecommend());
        targetGoods.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        targetGoods.setGoodsType(supplierGoods.getGoodsType());
        targetGoods.setParams(supplierGoods.getParams());
        goodsService.save(targetGoods);
        if (supplierGoods.getGoodsGalleryList() != null && !supplierGoods.getGoodsGalleryList().isEmpty()) {
            goodsGalleryService.add(supplierGoods.getGoodsGalleryList(), targetGoods.getId());
        }

        List<GoodsSku> targetSkus = new ArrayList<>();
        if (supplierGoods.getSkuList() == null || supplierGoods.getSkuList().isEmpty()) {
            return targetGoods;
        }
        for (GoodsSkuVO supplierSku : supplierGoods.getSkuList()) {
            targetSkus.add(buildTargetSku(agentStore, procurementOrder, targetGoods, supplierSku,
                    Objects.equals(supplierSku.getId(), procurementItem.getSkuId()) ? sourceOrderItem.getUnitPrice() : supplierSku.getCost()));
        }
        goodsSkuService.saveBatch(targetSkus);
        for (GoodsSku targetSku : targetSkus) {
            goodsSkuService.clearCache(targetSku.getId());
            esGoodsIndexService.getResetEsGoodsIndex(targetSku);
        }
        goodsService.updateStock(targetGoods.getId());
        return targetGoods;
    }

    private void cloneMissingSku(Store agentStore, ProcurementOrder procurementOrder, Goods targetGoods, GoodsVO supplierGoods,
                                 ProcurementOrderItem procurementItem, OrderItem sourceOrderItem) {
        if (supplierGoods.getSkuList() == null || supplierGoods.getSkuList().isEmpty()) {
            return;
        }
        GoodsSkuVO supplierSku = supplierGoods.getSkuList().stream()
                .filter(item -> Objects.equals(item.getId(), procurementItem.getSkuId()))
                .findFirst()
                .orElse(null);
        if (supplierSku == null) {
            return;
        }
        GoodsSku targetSku = buildTargetSku(agentStore, procurementOrder, targetGoods, supplierSku, sourceOrderItem.getUnitPrice());
        goodsSkuService.save(targetSku);
        goodsSkuService.clearCache(targetSku.getId());
        esGoodsIndexService.getResetEsGoodsIndex(targetSku);
        goodsService.updateStock(targetGoods.getId());
    }

    private GoodsSku buildTargetSku(Store agentStore, ProcurementOrder procurementOrder, Goods targetGoods,
                                    GoodsSkuVO supplierSku, Double costPrice) {
        GoodsSku targetSku = new GoodsSku(targetGoods);
        targetSku.setId(SnowFlake.getIdStr());
        targetSku.setGoodsId(targetGoods.getId());
        targetSku.setSpecs(supplierSku.getSpecs());
        targetSku.setSimpleSpecs(supplierSku.getSimpleSpecs());
        targetSku.setGoodsName(supplierSku.getGoodsName());
        targetSku.setSn(supplierSku.getSn());
        targetSku.setBarcode(supplierSku.getBarcode());
        targetSku.setBrandId(supplierSku.getBrandId());
        targetSku.setCategoryPath(supplierSku.getCategoryPath());
        targetSku.setGoodsUnit(supplierSku.getGoodsUnit());
        targetSku.setSellingPoint(supplierSku.getSellingPoint());
        targetSku.setWeight(supplierSku.getWeight());
        targetSku.setMarketEnable(GoodsStatusEnum.DOWN.name());
        targetSku.setIntro(supplierSku.getIntro());
        targetSku.setPrice(supplierSku.getPrice());
        targetSku.setCost(costPrice == null ? supplierSku.getCost() : costPrice);
        targetSku.setViewCount(0);
        targetSku.setBuyCount(0);
        targetSku.setVirtualSales(0);
        targetSku.setQuantity(0);
        targetSku.setGrade(100D);
        targetSku.setThumbnail(supplierSku.getThumbnail());
        targetSku.setBig(supplierSku.getBig());
        targetSku.setSmall(supplierSku.getSmall());
        targetSku.setOriginal(supplierSku.getOriginal());
        targetSku.setStoreCategoryPath(targetGoods.getStoreCategoryPath());
        targetSku.setCommentNum(0);
        targetSku.setStoreId(agentStore.getId());
        targetSku.setStoreName(agentStore.getStoreName());
        targetSku.setSourceStoreId(procurementOrder.getSupplierStoreId());
        targetSku.setSourceStoreName(procurementOrder.getSupplierStoreName());
        targetSku.setSourceGoodsId(supplierSku.getGoodsId());
        targetSku.setSourceSkuId(supplierSku.getId());
        targetSku.setFreightTemplateId(targetGoods.getTemplateId());
        targetSku.setAuthFlag(GoodsAuthEnum.PASS.name());
        targetSku.setAuthMessage(null);
        targetSku.setUnderMessage(null);
        targetSku.setSelfOperated(agentStore.getSelfOperated());
        targetSku.setMobileIntro(targetGoods.getMobileIntro());
        targetSku.setGoodsVideo(targetGoods.getGoodsVideo());
        targetSku.setRecommend(targetGoods.getRecommend());
        targetSku.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        targetSku.setGoodsType(targetGoods.getGoodsType());
        targetSku.setAlertQuantity(0);
        return targetSku;
    }
}
