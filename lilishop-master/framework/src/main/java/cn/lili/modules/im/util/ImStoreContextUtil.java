package cn.lili.modules.im.util;

import cn.hutool.core.util.StrUtil;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;

/**
 * IM 店铺端上下文工具
 *
 * <p>历史店铺 token 中常见的是 {@code storeId} 有值、{@code tenantId} 为空，
 * 因此 IM 店铺侧按租户隔离时需要优先取 {@code tenantId}，为空时回退到 {@code storeId}。</p>
 */
public final class ImStoreContextUtil {

    private ImStoreContextUtil() {
    }

    public static String getCurrentTenantOrStoreId() {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        if (StrUtil.isNotBlank(currentUser.getTenantId())) {
            return currentUser.getTenantId();
        }
        return currentUser.getStoreId();
    }
}
