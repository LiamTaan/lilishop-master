package cn.lili.controller.order;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.cart.entity.dto.AddCartDTO;
import cn.lili.modules.order.cart.entity.dto.CartCheckedDTO;
import cn.lili.modules.order.cart.entity.dto.CartCouponSelectDTO;
import cn.lili.modules.order.cart.entity.dto.CartReceiptSelectDTO;
import cn.lili.modules.order.cart.entity.dto.CartShippingAddressDTO;
import cn.lili.modules.order.cart.entity.dto.CartShippingMethodDTO;
import cn.lili.modules.order.cart.entity.dto.CartSkuDeleteDTO;
import cn.lili.modules.order.cart.entity.dto.CartSkuNumUpdateDTO;
import cn.lili.modules.order.cart.entity.dto.CartStoreAddressDTO;
import cn.lili.modules.order.cart.entity.dto.TradeDTO;
import cn.lili.modules.order.cart.entity.enums.CartTypeEnum;
import cn.lili.modules.order.cart.entity.vo.CreateTradeVO;
import cn.lili.modules.order.cart.entity.vo.TradeParams;
import cn.lili.modules.order.cart.service.CartService;
import cn.lili.modules.order.order.entity.vo.ReceiptVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 买家端，购物车接口
 *
 * @author Chopper
 * @since 2020/11/16 10:04 下午
 */
@Slf4j
@RestController
@Validated
@Tag(name = "买家端，购物车接口")
@RequestMapping("/buyer/trade/carts")
public class CartController {

    /**
     * 购物车
     */
    @Autowired
    private CartService cartService;

    @Autowired
    private Validator validator;

    @PostMapping
    @PreventDuplicateSubmissions
    @Operation(summary = "向购物车中添加一个产品(JSON)")
    public ResultMessage<Object> add(@RequestBody(required = false) AddCartDTO addCartDTO,
                                     HttpServletRequest request) {
        AddCartDTO payload = addCartDTO != null ? addCartDTO : legacyAddCartDTO(request);
        return this.doAdd(validateBody(payload));
    }

    private ResultMessage<Object> doAdd(AddCartDTO addCartDTO) {
        try {
            //读取选中的列表
            cartService.add(addCartDTO.getSkuId(), addCartDTO.getNum(), addCartDTO.getCartType(), false);
            return ResultUtil.success();
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }


    @Operation(summary = "获取购物车页面购物车详情")
    @GetMapping("/all")
    public ResultMessage<TradeDTO> cartAll() {
        return ResultUtil.data(this.cartService.getAllTradeDTO());
    }

    @Operation(summary = "获取购物车数量")   
    @GetMapping("/count")
    public ResultMessage<Long> cartCount(@RequestParam(required = false) Boolean checked) {
        return ResultUtil.data(this.cartService.getCartNum(checked));
    }

    @Operation(summary = "获取购物车可用优惠券数量")
    @Parameter(name = "way", description = "购物车购买：CART/立即购买：BUY_NOW/拼团购买：PINTUAN / 积分购买：POINT ", required = true)
    @GetMapping("/coupon/num")
    public ResultMessage<Long> cartCouponNum(String way) {
        return ResultUtil.data(this.cartService.getCanUseCoupon(CartTypeEnum.valueOf(way)));
    }

    @Operation(summary = "更新购物车中单个产品数量")
    @Parameter(name = "skuId", description = "产品id数组", required = true)
    @PostMapping("/sku/num/{skuId}")
    public ResultMessage<Object> update(@NotNull(message = "产品id不能为空") @PathVariable(name = "skuId") String skuId,
                                        @RequestBody(required = false) CartSkuNumUpdateDTO requestBody,
                                        HttpServletRequest request) {
        CartSkuNumUpdateDTO payload = requestBody != null ? requestBody : legacyCartSkuNumUpdateDTO(request);
        cartService.add(skuId, validateBody(payload).getNum(), CartTypeEnum.CART.name(), true);
        return ResultUtil.success();
    }


    @Operation(summary = "更新购物车中单个产品")
    @Parameter(name = "skuId", description = "产品id数组", required = true)
    @PostMapping("/sku/checked/{skuId}")
    public ResultMessage<Object> updateChecked(@NotNull(message = "产品id不能为空") @PathVariable(name = "skuId") String skuId,
                                               @RequestBody(required = false) CartCheckedDTO requestBody,
                                               HttpServletRequest request) {
        CartCheckedDTO payload = requestBody != null ? requestBody : legacyCartCheckedDTO(request);
        cartService.checked(skuId, validateBody(payload).getChecked());
        return ResultUtil.success();
    }


    @Operation(summary = "购物车选中设置")
    @PostMapping("/sku/checked")
    public ResultMessage<Object> updateAll(@RequestBody(required = false) CartCheckedDTO requestBody,
                                           HttpServletRequest request) {
        CartCheckedDTO payload = requestBody != null ? requestBody : legacyCartCheckedDTO(request);
        cartService.checkedAll(validateBody(payload).getChecked());
        return ResultUtil.success();
    }


    @Operation(summary = "批量设置某商家的商品为选中或不选中")
    @Parameter(name = "storeId", description = "卖家id", required = true)
    @PostMapping("/store/{storeId}")
    public ResultMessage<Object> updateStoreAll(@NotNull(message = "卖家id不能为空") @PathVariable(name = "storeId") String storeId,
                                                @RequestBody(required = false) CartCheckedDTO requestBody,
                                                HttpServletRequest request) {
        CartCheckedDTO payload = requestBody != null ? requestBody : legacyCartCheckedDTO(request);
        cartService.checkedStore(storeId, validateBody(payload).getChecked());
        return ResultUtil.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping()
    public ResultMessage<Object> clean() {
        cartService.clean();
        return ResultUtil.success();
    }


    @Operation(summary = "删除购物车中的一个或多个产品")
    @DeleteMapping("/sku/remove")
    public ResultMessage<Object> delete(@RequestBody(required = false) CartSkuDeleteDTO requestBody,
                                        HttpServletRequest request) {
        CartSkuDeleteDTO payload = requestBody != null ? requestBody : legacyCartSkuDeleteDTO(request);
        cartService.delete(validateBody(payload).getSkuIds());
        return ResultUtil.success();
    }

    @Operation(summary = "获取结算页面购物车详情")
    @Parameter(name = "way", description = "购物车购买：CART/立即购买：BUY_NOW/拼团购买：PINTUAN / 积分购买：POINT", required = true)
    @GetMapping("/checked")
    public ResultMessage<TradeDTO> cartChecked(@NotNull(message = "读取选中列表") String way) {
        try {
            //读取选中的列表
            return ResultUtil.data(this.cartService.getCheckedTradeDTO(CartTypeEnum.valueOf(way)));
        } catch (ServiceException se) {
            log.error(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @Operation(summary = "选择收货地址")
    @PutMapping("/shippingAddress")
    public ResultMessage<Object> shippingAddress(@RequestBody @Valid CartShippingAddressDTO requestBody) {
        try {
            cartService.shippingAddress(requestBody.getShippingAddressId(), requestBody.getWay());
            return ResultUtil.success();
        } catch (ServiceException se) {
            log.error(ResultCode.SHIPPING_NOT_APPLY.message(), se);
            throw new ServiceException(ResultCode.SHIPPING_NOT_APPLY);
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @Hidden
    @GetMapping("/shippingAddress")
    public ResultMessage<Object> legacyShippingAddress(@NotNull(message = "收货地址ID不能为空") String shippingAddressId,
                                                       String way) {
        return shippingAddress(validateBody(legacyCartShippingAddressDTO(shippingAddressId, way)));
    }

    @Operation(summary = "选择自提地址")
    @PutMapping("/storeAddress")
    public ResultMessage<Object> shippingSelfPickAddress(@RequestBody @Valid CartStoreAddressDTO requestBody) {
        try {
            cartService.shippingSelfAddress(requestBody.getStoreAddressId(), requestBody.getWay());
            return ResultUtil.success();
        } catch (ServiceException se) {
            log.error(ResultCode.SHIPPING_NOT_APPLY.message(), se);
            throw new ServiceException(ResultCode.SHIPPING_NOT_APPLY);
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @Hidden
    @GetMapping("/storeAddress")
    public ResultMessage<Object> legacyShippingSelfPickAddress(@NotNull(message = "自提地址ID不能为空") String storeAddressId,
                                                               String way) {
        return shippingSelfPickAddress(validateBody(legacyCartStoreAddressDTO(storeAddressId, way)));
    }

    @Operation(summary = "选择配送方式")
    @PutMapping("/shippingMethod")
    public ResultMessage<Object> shippingMethod(@RequestBody(required = false) CartShippingMethodDTO requestBody,
                                                HttpServletRequest request) {
        CartShippingMethodDTO payload = requestBody != null ? requestBody : legacyCartShippingMethodDTO(request);
        try {
            CartShippingMethodDTO validPayload = validateBody(payload);
            cartService.shippingMethod(validPayload.getShippingMethod(), validPayload.getWay());
            return ResultUtil.success();
        } catch (ServiceException se) {
            log.error(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @Operation(summary = "获取用户可选择的物流方式")
    @Parameter(name = "way", description = "购物车类型 ", required = true)
    @GetMapping("/shippingMethodList")
    public ResultMessage<Object> shippingMethodList(String way) {
        try {
            return ResultUtil.data(cartService.shippingMethodList(way));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error(ResultCode.ERROR);
        }
    }

    @Operation(summary = "选择发票")
    @PutMapping("/select/receipt")
    public ResultMessage<Object> selectReceipt(@RequestBody @Valid CartReceiptSelectDTO requestBody) {
        this.cartService.shippingReceipt(requestBody.getReceipt(), requestBody.getWay());
        return ResultUtil.success();
    }

    @Hidden
    @GetMapping("/select/receipt")
    public ResultMessage<Object> legacySelectReceipt(String way, ReceiptVO receiptVO) {
        CartReceiptSelectDTO requestBody = new CartReceiptSelectDTO();
        requestBody.setWay(way);
        requestBody.setReceipt(receiptVO);
        return selectReceipt(validateBody(requestBody));
    }

    @Operation(summary = "选择优惠券")
    @PutMapping("/select/coupon")
    public ResultMessage<Object> selectCoupon(@RequestBody @Valid CartCouponSelectDTO requestBody) {
        this.cartService.selectCoupon(requestBody.getMemberCouponId(), requestBody.getWay(), requestBody.getUsed());
        return ResultUtil.success();
    }

    @Hidden
    @GetMapping("/select/coupon")
    public ResultMessage<Object> legacySelectCoupon(String way,
                                                    @NotNull(message = "优惠券id不能为空") String memberCouponId,
                                                    boolean used) {
        CartCouponSelectDTO requestBody = new CartCouponSelectDTO();
        requestBody.setWay(way);
        requestBody.setMemberCouponId(memberCouponId);
        requestBody.setUsed(used);
        return selectCoupon(validateBody(requestBody));
    }


    @PreventDuplicateSubmissions
    @Operation(summary = "创建交易")
    @PostMapping(path = "/create/trade")
    public ResultMessage<CreateTradeVO> crateTrade(@RequestBody TradeParams tradeParams) {
        try {
            //读取选中的列表
            return ResultUtil.data(this.cartService.createTrade(tradeParams));
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.ORDER_ERROR.message(), e);
            throw e;
        }
    }

    private AddCartDTO legacyAddCartDTO(HttpServletRequest request) {
        AddCartDTO payload = new AddCartDTO();
        payload.setSkuId(request.getParameter("skuId"));
        payload.setNum(parseInteger(request.getParameter("num"), "购买数量"));
        payload.setCartType(request.getParameter("cartType"));
        return payload;
    }

    private CartSkuNumUpdateDTO legacyCartSkuNumUpdateDTO(HttpServletRequest request) {
        CartSkuNumUpdateDTO payload = new CartSkuNumUpdateDTO();
        payload.setNum(parseInteger(request.getParameter("num"), "产品数量"));
        return payload;
    }

    private CartCheckedDTO legacyCartCheckedDTO(HttpServletRequest request) {
        CartCheckedDTO payload = new CartCheckedDTO();
        payload.setChecked(parseBoolean(request.getParameter("checked"), "是否选中"));
        return payload;
    }

    private CartSkuDeleteDTO legacyCartSkuDeleteDTO(HttpServletRequest request) {
        CartSkuDeleteDTO payload = new CartSkuDeleteDTO();
        payload.setSkuIds(request.getParameterValues("skuIds"));
        return payload;
    }

    private CartShippingAddressDTO legacyCartShippingAddressDTO(String shippingAddressId, String way) {
        CartShippingAddressDTO payload = new CartShippingAddressDTO();
        payload.setShippingAddressId(shippingAddressId);
        payload.setWay(way);
        return payload;
    }

    private CartStoreAddressDTO legacyCartStoreAddressDTO(String storeAddressId, String way) {
        CartStoreAddressDTO payload = new CartStoreAddressDTO();
        payload.setStoreAddressId(storeAddressId);
        payload.setWay(way);
        return payload;
    }

    private CartShippingMethodDTO legacyCartShippingMethodDTO(HttpServletRequest request) {
        CartShippingMethodDTO payload = new CartShippingMethodDTO();
        payload.setShippingMethod(request.getParameter("shippingMethod"));
        payload.setWay(request.getParameter("way"));
        return payload;
    }

    private Integer parseInteger(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, fieldName + "格式不正确");
        }
    }

    private Boolean parseBoolean(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }
        throw new ServiceException(ResultCode.PARAMS_ERROR, fieldName + "格式不正确");
    }

    private <T> T validateBody(T payload) {
        Set<ConstraintViolation<T>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .collect(Collectors.joining(", "));
            throw new ServiceException(ResultCode.PARAMS_ERROR, message);
        }
        return payload;
    }
}
