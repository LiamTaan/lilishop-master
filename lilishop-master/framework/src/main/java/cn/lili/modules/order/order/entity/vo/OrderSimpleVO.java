package cn.lili.modules.order.order.entity.vo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.security.sensitive.Sensitive;
import cn.lili.common.security.sensitive.enums.SensitiveStrategy;
import cn.lili.modules.order.cart.entity.enums.DeliveryMethodEnum;
import cn.lili.modules.order.order.entity.enums.OrderItemAfterSaleStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderPromotionTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单简略信息
 * 用于订单列表查看
 *
 * @author Chopper
 * @since 2020-08-17 20:28
 */
@Data
public class OrderSimpleVO {

    @Schema(description = "sn")
    private String sn;

    @Schema(description = "总价格")
    private Double flowPrice;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * @see cn.lili.modules.order.order.entity.enums.OrderStatusEnum
     */
    @Schema(description = "订单状态")
    private String orderStatus;

    /**
     * @see cn.lili.modules.order.order.entity.enums.PayStatusEnum
     */
    @Schema(description = "付款状态")
    private String payStatus;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paymentTime;

    @Schema(description = "用户ID")
    private String memberId;

    @Schema(description = "用户名")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String memberName;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺logo")
    private String storeLogo;

    /**
     * @see ClientTypeEnum
     */
    @Schema(description = "订单来源")
    private String clientType;

    /**
     * 子订单信息
     */
    private List<OrderItemVO> orderItems;

    @Schema(hidden = true, description = "item goods_id")
    @Setter
    private String groupGoodsId;

    @Schema(hidden = true, description = "item sku id")
    @Setter
    private String groupSkuId;

    @Schema(hidden = true, description = "item 数量")
    @Setter
    private String groupNum;

    @Schema(hidden = true, description = "item 图片")
    @Setter
    private String groupImages;

    @Schema(hidden = true, description = "item 名字")
    @Setter
    private String groupName;

    @Schema(hidden = true, description = "item 编号")
    @Setter
    private String groupOrderItemsSn;

    @Schema(hidden = true, description = "item 商品价格")
    @Setter
    private String groupGoodsPrice;
    /**
     * @see cn.lili.modules.order.order.entity.enums.OrderItemAfterSaleStatusEnum
     */
    @Schema(hidden = true, description = "item 售后状态", allowableValues = "NOT_APPLIED(未申请),ALREADY_APPLIED(已申请),EXPIRED(已失效不允许申请售后)")
    @Setter
    private String groupAfterSaleStatus;

    /**
     * @see cn.lili.modules.order.order.entity.enums.OrderComplaintStatusEnum
     */
    @Schema(hidden = true, description = "item 投诉状态")
    @Setter
    private String groupComplainStatus;

    /**
     * @see cn.lili.modules.order.order.entity.enums.CommentStatusEnum
     */
    @Schema(hidden = true, description = "item 评价状态")
    @Setter
    private String groupCommentStatus;


    /**
     * @see cn.lili.modules.order.order.entity.enums.OrderTypeEnum
     */
    @Schema(description = "订单类型")
    private String orderType;

    /**
     * @see cn.lili.modules.order.order.entity.enums.DeliverStatusEnum
     */
    @Schema(description = "货运状态")
    private String deliverStatus;

    /**
     * @see DeliveryMethodEnum
     */
    @Schema(description = "配送方式")
    private String deliveryMethod;

    @Schema(description = "提货码")
    private String verificationCode;

    @Schema(description = "自提点地址")
    private String storeAddressPath;

    @Schema(description = "自提点电话")
    private String storeAddressMobile;

    @Schema(description = "自提点经纬度")
    private String storeAddressCenter;

    /**
     * @see cn.lili.modules.order.order.entity.enums.OrderPromotionTypeEnum
     */
    @Schema(description = "订单促销类型")
    private String orderPromotionType;

    @Schema(description = "是否退款")
    private String groupIsRefund;

    @Schema(description = "退款金额")
    private String groupRefundPrice;

    @Schema(description = "卖家订单备注")
    private String sellerRemark;

    public List<OrderItemVO> getOrderItems() {
        if (CharSequenceUtil.isEmpty(groupGoodsId)) {
            return new ArrayList<>();
        }
        List<OrderItemVO> orderItemVOS = new ArrayList<>();


        String[] goodsId = groupGoodsId.split(",");

        for (int i = 0; i < goodsId.length; i++) {
            orderItemVOS.add(this.getOrderItemVO(i));
        }
        return orderItemVOS;

    }

    private OrderItemVO getOrderItemVO(int i) {
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setGoodsId(groupGoodsId.split(",")[i]);
        if (CharSequenceUtil.isNotEmpty(groupOrderItemsSn)) {
            orderItemVO.setSn(groupOrderItemsSn.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupSkuId)) {
            orderItemVO.setSkuId(groupSkuId.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupName)) {
            orderItemVO.setName(groupName.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupNum) && groupNum.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setNum(groupNum.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupImages) && groupImages.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setImage(groupImages.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupAfterSaleStatus) && groupAfterSaleStatus.split(",").length == groupGoodsId.split(",").length) {
            if (!OrderPromotionTypeEnum.isCanAfterSale(this.orderPromotionType)) {
                orderItemVO.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.EXPIRED.name());
            } else {
                orderItemVO.setAfterSaleStatus(groupAfterSaleStatus.split(",")[i]);
            }
        }
        if (CharSequenceUtil.isNotEmpty(groupComplainStatus) && groupComplainStatus.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setComplainStatus(groupComplainStatus.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupCommentStatus) && groupCommentStatus.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setCommentStatus(groupCommentStatus.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupGoodsPrice) && groupGoodsPrice.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setGoodsPrice(Double.parseDouble(groupGoodsPrice.split(",")[i]));
        }
        if (CharSequenceUtil.isNotEmpty(groupIsRefund) && groupIsRefund.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setIsRefund(groupIsRefund.split(",")[i]);
        }
        if (CharSequenceUtil.isNotEmpty(groupRefundPrice) && groupRefundPrice.split(",").length == groupGoodsId.split(",").length) {
            orderItemVO.setRefundPrice(groupRefundPrice.split(",")[i]);
        }
        return orderItemVO;
    }

    /**
     * 初始化自身状态
     */
    public AllowOperation getAllowOperationVO() {
        //设置订单的可操作状态
        return new AllowOperation(this);
    }

    @Schema(description = "订单编号")
    public String getOrderSn() {
        return this.sn;
    }

    @Schema(description = "履约类型 LOGISTICS/PICKUP/VERIFICATION")
    public String getFulfillmentType() {
        if (OrderStatusEnum.TAKE.name().equals(this.orderStatus) || "VIRTUAL".equals(this.orderType)) {
            return "VERIFICATION";
        }
        if (DeliveryMethodEnum.SELF_PICK_UP.name().equals(this.deliveryMethod)
                || OrderStatusEnum.STAY_PICKED_UP.name().equals(this.orderStatus)) {
            return "PICKUP";
        }
        return "LOGISTICS";
    }

    @Schema(description = "面向前端的展示状态")
    public String getDisplayStatus() {
        if (isVerifyAction()) {
            return "待核销";
        }
        if (isReceiveAction()) {
            return "待收货";
        }
        try {
            return OrderStatusEnum.valueOf(this.orderStatus).description();
        } catch (Exception e) {
            return this.orderStatus;
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

    public String getGroupAfterSaleStatus() {
        // 不可售后的订单类型集合
        if (!OrderPromotionTypeEnum.isCanAfterSale(this.orderPromotionType)) {
            return OrderItemAfterSaleStatusEnum.EXPIRED.name();
        }
        return groupAfterSaleStatus;
    }

    private boolean isReceiveAction() {
        return "LOGISTICS".equals(this.getFulfillmentType()) && OrderStatusEnum.DELIVERED.name().equals(this.orderStatus);
    }

    private boolean isVerifyAction() {
        return ("PICKUP".equals(this.getFulfillmentType()) && OrderStatusEnum.STAY_PICKED_UP.name().equals(this.orderStatus))
                || ("VERIFICATION".equals(this.getFulfillmentType()) && OrderStatusEnum.TAKE.name().equals(this.orderStatus));
    }
}
