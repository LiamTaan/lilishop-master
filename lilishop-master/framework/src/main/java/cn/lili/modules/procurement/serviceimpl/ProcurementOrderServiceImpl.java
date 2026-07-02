package cn.lili.modules.procurement.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.utils.BeanUtil;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.dos.ProcurementOrderItem;
import cn.lili.modules.procurement.entity.params.ProcurementOrderSearchParams;
import cn.lili.modules.procurement.entity.vos.ProcurementOrderVO;
import cn.lili.modules.procurement.mapper.ProcurementOrderMapper;
import cn.lili.modules.procurement.service.ProcurementOrderItemService;
import cn.lili.modules.procurement.service.ProcurementOrderService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购单业务实现
 * 采购单由订单链路自动生成，当前服务仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
@Service
public class ProcurementOrderServiceImpl extends ServiceImpl<ProcurementOrderMapper, ProcurementOrder> implements ProcurementOrderService {

    @Autowired
    private ProcurementOrderItemService orderItemService;
    @Autowired
    private StoreService storeService;

    @Override
    public ProcurementOrderVO getDetail(String id) {
        ProcurementOrder order = this.getById(id);
        assertCurrentStoreOrder(order);
        ProcurementOrderVO vo = new ProcurementOrderVO();
        BeanUtil.copyProperties(order, vo);
        List<ProcurementOrderItem> items = orderItemService.list(new LambdaQueryWrapper<ProcurementOrderItem>().eq(ProcurementOrderItem::getProcurementOrderId, id));
        vo.setItems(items);
        return vo;
    }

    @Override
    public IPage<ProcurementOrder> page(ProcurementOrderSearchParams params) {
        LambdaQueryWrapper<ProcurementOrder> wrapper = Wrappers.lambdaQuery();
        AuthUser user = UserContext.getCurrentUser();
        if (isStoreUser(user)) {
            assertAgentStoreUser(user);
            wrapper.eq(ProcurementOrder::getStoreId, user.getStoreId());
        }
        if (CharSequenceUtil.isNotEmpty(params.getOrderSn())) {
            wrapper.eq(ProcurementOrder::getOrderSn, params.getOrderSn());
        }
        if (CharSequenceUtil.isNotEmpty(params.getStatus())) {
            wrapper.eq(ProcurementOrder::getStatus, params.getStatus());
        }
        if (params.getStartCreateTime() != null) {
            wrapper.ge(ProcurementOrder::getCreateTime, params.getStartCreateTime());
        }
        if (params.getEndCreateTime() != null) {
            wrapper.le(ProcurementOrder::getCreateTime, params.getEndCreateTime());
        }
        if (params.getStartAuditTime() != null) {
            wrapper.ge(ProcurementOrder::getAuditTime, params.getStartAuditTime());
        }
        if (params.getEndAuditTime() != null) {
            wrapper.le(ProcurementOrder::getAuditTime, params.getEndAuditTime());
        }
        if (CharSequenceUtil.isNotEmpty(params.getGoodsName())) {
            List<String> orderIds = orderItemService.list(Wrappers.<ProcurementOrderItem>lambdaQuery()
                    .like(ProcurementOrderItem::getGoodsName, params.getGoodsName()))
                    .stream().map(ProcurementOrderItem::getProcurementOrderId).distinct().collect(Collectors.toList());
            if (!orderIds.isEmpty()) {
                wrapper.in(ProcurementOrder::getId, orderIds);
            } else {
                wrapper.eq(ProcurementOrder::getId, "0");
            }
        }
        wrapper.orderByDesc(ProcurementOrder::getCreateTime);
        return this.page(PageUtil.initPage(params), wrapper);
    }

    private void assertCurrentStoreOrder(ProcurementOrder order) {
        AuthUser user = UserContext.getCurrentUser();
        if (order == null) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        if (isStoreUser(user)) {
            assertAgentStoreUser(user);
            if (!user.getStoreId().equals(order.getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
        }
    }

    private void assertAgentStoreUser(AuthUser user) {
        if (user == null || CharSequenceUtil.isEmpty(user.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        Store store = storeService.getById(user.getStoreId());
        if (store == null || !StoreBizTypeEnum.AGENT.name().equalsIgnoreCase(store.getStoreType())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }

    private boolean isStoreUser(AuthUser user) {
        return user != null && UserEnums.STORE.equals(user.getRole());
    }
}
