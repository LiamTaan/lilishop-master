package cn.lili.modules.order.order.entity.vo;


import cn.lili.modules.order.cart.entity.enums.DeliveryMethodEnum;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dos.OrderItem;
import cn.lili.modules.order.order.entity.dos.Receipt;
import cn.lili.modules.order.order.entity.enums.DeliverStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.PayStatusEnum;
import cn.lili.modules.order.trade.entity.dos.OrderLog;
import cn.lili.modules.payment.entity.enums.PaymentMethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单详情VO
 *
 * @author Chopper
 * @since 2020/11/17 7:29 下午
 */
@Data
@NoArgsConstructor
public class OrderDetailVO implements Serializable {


    private static final long serialVersionUID = -6293102172184734928L;

    /**
     * 订单
     */
    private Order order;

    /**
     * 子订单信息
     */
    private List<OrderItem> orderItems;

    /**
     * 订单状态
     */
    private String orderStatusValue;

    /**
     * 付款状态
     */
    private String payStatusValue;

    /**
     * 物流状态
     */
    private String deliverStatusValue;

    /**
     * 物流类型
     */
    private String deliveryMethodValue;

    /**
     * 支付类型
     */
    private String paymentMethodValue;

    /**
     * 发票
     */
    private Receipt receipt;

    /**
     * 获取订单日志
     */
    private List<OrderLog> orderLogs;
    @Schema(description = "价格详情")
    private String priceDetail;

    public OrderDetailVO(Order order, List<OrderItem> orderItems, List<OrderLog> orderLogs, Receipt receipt) {
        this.order = order;
        this.orderItems = orderItems;
        this.orderLogs = orderLogs;
        this.receipt = receipt;
    }

    /**
     * 可操作类型
     */
    public AllowOperation getAllowOperationVO() {
        return new AllowOperation(this.order);
    }

    @Schema(description = "履约类型 LOGISTICS/PICKUP/VERIFICATION")
    public String getFulfillmentType() {
        if (order == null) {
            return "LOGISTICS";
        }
        if (OrderStatusEnum.TAKE.name().equals(order.getOrderStatus()) || "VIRTUAL".equals(order.getOrderType())) {
            return "VERIFICATION";
        }
        if (DeliveryMethodEnum.SELF_PICK_UP.name().equals(order.getDeliveryMethod())
                || OrderStatusEnum.STAY_PICKED_UP.name().equals(order.getOrderStatus())) {
            return "PICKUP";
        }
        return "LOGISTICS";
    }

    @Schema(description = "面向前端的展示状态")
    public String getDisplayStatus() {
        if (order == null) {
            return "";
        }
        if (isVerifyAction()) {
            return "待核销";
        }
        if (isReceiveAction()) {
            return "待收货";
        }
        try {
            return OrderStatusEnum.valueOf(order.getOrderStatus()).description();
        } catch (Exception e) {
            return order.getOrderStatus();
        }
    }

    @Schema(description = "主操作 RECEIVE/VERIFY/NONE")
    public String getPrimaryAction() {
        if (isReceiveAction()) {
            return "RECEIVE";
        }
        if (isVerifyAction()) {
            return "VERIFY";
        }
        return "NONE";
    }

    @Schema(description = "主操作是否可执行")
    public Boolean getPrimaryActionEnabled() {
        return isReceiveAction() || isVerifyAction();
    }

    @Schema(description = "提货码")
    public String getVerificationCode() {
        return order == null ? null : order.getVerificationCode();
    }

    @Schema(description = "自提点地址")
    public String getStoreAddressPath() {
        return order == null ? null : order.getStoreAddressPath();
    }

    @Schema(description = "自提点电话")
    public String getStoreAddressMobile() {
        return order == null ? null : order.getStoreAddressMobile();
    }

    public String getOrderStatusValue() {
        try {
            return OrderStatusEnum.valueOf(order.getOrderStatus()).description();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPayStatusValue() {
        try {
            return PayStatusEnum.valueOf(order.getPayStatus()).description();
        } catch (Exception e) {
            return "";
        }

    }

    public String getDeliverStatusValue() {
        try {
            return DeliverStatusEnum.valueOf(order.getDeliverStatus()).getDescription();
        } catch (Exception e) {
            return "";
        }
    }

    public String getDeliveryMethodValue() {
        try {
            return DeliveryMethodEnum.valueOf(order.getDeliveryMethod()).getDescription();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPaymentMethodValue() {
        try {
            return PaymentMethodEnum.valueOf(order.getPaymentMethod()).paymentName();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isReceiveAction() {
        return order != null && "LOGISTICS".equals(this.getFulfillmentType()) && OrderStatusEnum.DELIVERED.name().equals(order.getOrderStatus());
    }

    private boolean isVerifyAction() {
        return order != null
                && (("PICKUP".equals(this.getFulfillmentType()) && OrderStatusEnum.STAY_PICKED_UP.name().equals(order.getOrderStatus()))
                || ("VERIFICATION".equals(this.getFulfillmentType()) && OrderStatusEnum.TAKE.name().equals(order.getOrderStatus())));
    }
}
