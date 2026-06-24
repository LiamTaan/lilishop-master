package cn.lili.modules.store.support;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.search.entity.dto.EsGoodsSearchDTO;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.entity.vos.StoreSearchParams;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 买家端店铺池范围支持
 *
 * @author OpenAI
 */
@Component
public class BuyerStoreScopeSupport {

    private static final String NO_VISIBLE_STORE_ID = "__NO_VISIBLE_STORE__";

    @Autowired
    private StoreService storeService;

    public void applyStoreScope(StoreSearchParams searchParams) {
        applyStoreScope(searchParams, getCurrentIdentityCode());
    }

    public void applyStoreScope(StoreSearchParams searchParams, LoginIdentityCodeEnum identityCode) {
        if (searchParams == null) {
            return;
        }
        String bizType = resolveVisibleStoreBizType(identityCode);
        if (CharSequenceUtil.isNotBlank(bizType)) {
            searchParams.setBizType(bizType);
        }
    }

    public void applyGoodsScope(GoodsSearchParams goodsSearchParams) {
        applyGoodsScope(goodsSearchParams, getCurrentIdentityCode());
    }

    public void applyGoodsScope(GoodsSearchParams goodsSearchParams, LoginIdentityCodeEnum identityCode) {
        if (goodsSearchParams == null) {
            return;
        }
        String bizType = resolveVisibleStoreBizType(identityCode);
        if (CharSequenceUtil.isBlank(bizType)) {
            return;
        }
        goodsSearchParams.setStoreIds(buildVisibleStoreIds(bizType));
    }

    public void applyEsGoodsScope(EsGoodsSearchDTO searchDTO) {
        applyEsGoodsScope(searchDTO, getCurrentIdentityCode());
    }

    public void applyEsGoodsScope(EsGoodsSearchDTO searchDTO, LoginIdentityCodeEnum identityCode) {
        if (searchDTO == null) {
            return;
        }
        String bizType = resolveVisibleStoreBizType(identityCode);
        if (CharSequenceUtil.isBlank(bizType)) {
            return;
        }
        searchDTO.setStoreIds(buildVisibleStoreIds(bizType));
    }

    public String resolveVisibleStoreBizType(LoginIdentityCodeEnum identityCode) {
        if (identityCode == null) {
            return null;
        }
        return switch (identityCode) {
            case CONSUMER -> StoreBizTypeEnum.AGENT.name();
            case AGENT -> StoreBizTypeEnum.SUPPLIER.name();
            default -> null;
        };
    }

    public LoginIdentityCodeEnum getCurrentIdentityCode() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null || authUser.getRole() != UserEnums.MEMBER) {
            return null;
        }
        return authUser.getIdentityCode();
    }

    private List<String> buildVisibleStoreIds(String bizType) {
        List<String> storeIds = storeService.list(new LambdaQueryWrapper<Store>()
                        .eq(Store::getStoreType, bizType)
                        .eq(Store::getAuditStatus, StoreAuditStatusEnum.APPROVED.name())
                        .eq(Store::getDeleteFlag, false))
                .stream()
                .map(Store::getId)
                .distinct()
                .collect(Collectors.toList());
        if (storeIds.isEmpty()) {
            return Collections.singletonList(NO_VISIBLE_STORE_ID);
        }
        return storeIds;
    }
}
