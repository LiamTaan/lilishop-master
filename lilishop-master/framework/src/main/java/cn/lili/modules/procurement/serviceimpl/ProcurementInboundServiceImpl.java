package cn.lili.modules.procurement.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.params.ProcurementInboundSearchParams;
import cn.lili.modules.procurement.mapper.ProcurementInboundMapper;
import cn.lili.modules.procurement.service.ProcurementInboundService;
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

/**
 * 采购入库业务实现
 * 采购入库由订单完成链路自动生成，当前服务仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
@Service
public class ProcurementInboundServiceImpl extends ServiceImpl<ProcurementInboundMapper, ProcurementInbound> implements ProcurementInboundService {

    @Autowired
    private StoreService storeService;

    @Override
    public IPage<ProcurementInbound> page(ProcurementInboundSearchParams params) {
        LambdaQueryWrapper<ProcurementInbound> wrapper = Wrappers.lambdaQuery();
        AuthUser user = UserContext.getCurrentUser();
        if (isStoreUser(user)) {
            assertAgentStoreUser(user);
            wrapper.eq(ProcurementInbound::getStoreId, user.getStoreId());
        }
        if (CharSequenceUtil.isNotEmpty(params.getInboundSn())) {
            wrapper.eq(ProcurementInbound::getInboundSn, params.getInboundSn());
        }
        if (CharSequenceUtil.isNotEmpty(params.getProcurementOrderId())) {
            wrapper.eq(ProcurementInbound::getProcurementOrderId, params.getProcurementOrderId());
        }
        wrapper.orderByDesc(ProcurementInbound::getCreateTime);
        return this.page(PageUtil.initPage(params), wrapper);
    }

    @Override
    public ProcurementInbound getDetail(String id) {
        ProcurementInbound inbound = this.getById(id);
        assertCurrentStoreInbound(inbound);
        return inbound;
    }

    private void assertCurrentStoreInbound(ProcurementInbound inbound) {
        AuthUser user = UserContext.getCurrentUser();
        if (inbound == null) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        if (isStoreUser(user)) {
            assertAgentStoreUser(user);
            if (!user.getStoreId().equals(inbound.getStoreId())) {
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
