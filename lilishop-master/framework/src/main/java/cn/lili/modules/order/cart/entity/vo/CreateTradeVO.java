package cn.lili.modules.order.cart.entity.vo;

import cn.lili.common.utils.BeanUtil;
import cn.lili.modules.order.order.entity.dos.Trade;
import cn.lili.modules.order.order.entity.vo.OrderVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 创建交易返回对象
 *
 * @author Codex
 * @since 2026-06-30
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "创建交易返回对象")
public class CreateTradeVO extends Trade {

    private static final long serialVersionUID = -8490161948451006647L;

    @Schema(description = "订单编号，单店铺场景直接返回")
    private String orderSn;

    @Schema(description = "订单编号列表，多店铺场景可能存在多个")
    private List<String> orderSns;

    public CreateTradeVO(Trade trade, List<OrderVO> orders) {
        BeanUtil.copyProperties(trade, this);
        List<String> currentOrderSns = orders == null ? Collections.emptyList()
                : orders.stream().map(OrderVO::getSn).collect(Collectors.toList());
        this.setOrderSns(currentOrderSns);
        if (currentOrderSns.size() == 1) {
            this.setOrderSn(currentOrderSns.get(0));
        }
    }
}
