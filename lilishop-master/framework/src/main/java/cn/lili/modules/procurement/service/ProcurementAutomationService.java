package cn.lili.modules.procurement.service;

import cn.lili.modules.order.aftersale.entity.dos.AfterSale;

/**
 * 订单驱动的采购自动化服务。
 *
 * @author OpenAI
 */
public interface ProcurementAutomationService {

    /**
     * 支付成功后自动生成采购单。
     *
     * @param orderSn 订单编号
     */
    void createProcurementByPaidOrder(String orderSn);

    /**
     * 订单取消后自动关闭采购单。
     *
     * @param orderSn 订单编号
     * @param reason  关闭原因
     */
    void closeProcurementByCancelledOrder(String orderSn, String reason);

    /**
     * 代理商确认收货/订单完成后自动生成采购入库。
     *
     * @param orderSn 订单编号
     */
    void inboundProcurementByCompletedOrder(String orderSn);

    /**
     * 售后完成后回写采购侧库存结果。
     *
     * @param afterSale 售后单
     */
    void applyCompletedAfterSale(AfterSale afterSale);
}
