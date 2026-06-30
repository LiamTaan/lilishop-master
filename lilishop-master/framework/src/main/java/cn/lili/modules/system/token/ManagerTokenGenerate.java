package cn.lili.modules.system.token;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.PermissionEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.Token;
import cn.lili.common.security.token.TokenUtil;
import cn.lili.common.security.token.base.AbstractTokenGenerate;
import cn.lili.modules.permission.entity.dos.AdminUser;
import cn.lili.modules.permission.entity.vo.UserMenuVO;
import cn.lili.modules.permission.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员token生成
 *
 * @author Chopper
 * @version v4.0
 * @since 2020/11/16 10:51
 */
@Component
public class ManagerTokenGenerate extends AbstractTokenGenerate<AdminUser> {

    @Autowired
    private TokenUtil tokenUtil;
    @Autowired
    private MenuService menuService;
    @Autowired
    private Cache cache;


    @Override
    public Token createToken(AdminUser adminUser, Boolean longTerm) {
        AuthUser authUser = AuthUser.builder()
                .username(adminUser.getUsername())
                .id(adminUser.getId())
                .face(adminUser.getAvatar())
                .role(UserEnums.MANAGER)
                .nickName(adminUser.getNickName())
                .isSuper(adminUser.getIsSuper())
                .longTerm(longTerm)
                .build();

        List<UserMenuVO> userMenuVOList = menuService.findAllMenu(authUser.getId());
        //缓存权限列表
        cache.put(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER) + authUser.getId(), this.permissionList(userMenuVOList));

        return tokenUtil.createToken(authUser);
    }

    @Override
    public Token refreshToken(String refreshToken) {
        return tokenUtil.refreshToken(refreshToken);
    }

    /**
     * 获取用户权限
     *
     * @param userMenuVOList
     * @return
     */
    public Map<String, List<String>> permissionList(List<UserMenuVO> userMenuVOList) {
        Map<String, List<String>> permission = new HashMap<>(2);

        List<String> superPermissions = new ArrayList<>();
        List<String> queryPermissions = new ArrayList<>();
        initPermission(superPermissions, queryPermissions);

        //循环权限菜单
        if (userMenuVOList != null && !userMenuVOList.isEmpty()) {
            userMenuVOList.forEach(menu -> {
                //循环菜单，赋予用户权限
                if (CharSequenceUtil.isNotEmpty(menu.getPermission())) {
                    List<String> permissionUrls = normalizePermissionUrls(menu);
                    //for循环路径集合
                    for (String url : permissionUrls) {
                        if (!queryPermissions.contains(url)) {
                            queryPermissions.add(url);
                        }
                        // 当前管理端角色授权界面未区分“仅查看”与“可操作”，
                        // 角色勾选菜单后默认赋予对应接口的操作权限，避免出现菜单可见但写接口被统一拦截。
                        if (Boolean.TRUE.equals(menu.getSuper()) || !superPermissions.contains(url)) {
                            if (!superPermissions.contains(url)) {
                                superPermissions.add(url);
                            }
                        }
                    }
                }
                //去除重复的权限
                queryPermissions.removeAll(superPermissions);
            });
        }
        permission.put(PermissionEnum.SUPER.name(), superPermissions);
        permission.put(PermissionEnum.QUERY.name(), queryPermissions);
        return permission;
    }

    /**
     * 初始赋予的权限，查看权限包含首页流量统计权限，
     * 超级权限包含个人信息维护，密码修改权限
     *
     * @param superPermissions 超级权限
     * @param queryPermissions 查询权限
     */
    void initPermission(List<String> superPermissions, List<String> queryPermissions) {
        //TODO 用户信息维护--操作权限
        //获取当前登录用户
        superPermissions.add("/manager/passport/user/info*");
        //修改用户资料
        superPermissions.add("/manager/passport/user/edit*");
        //修改密码
        superPermissions.add("/manager/passport/user/editPassword*");
        //退出
        superPermissions.add("/manager/passport/user/logout*");

        //统计查看权限
        queryPermissions.add("/manager/statistics*");
        //菜单查看权限
        queryPermissions.add("/manager/permission/menu*");
        //商品分类查看权限
        queryPermissions.add("/manager/goods/category*");
        //查看地区接口
        queryPermissions.add("/manager/setting/region*");

    }

    private List<String> normalizePermissionUrls(UserMenuVO menu) {
        Set<String> permissionUrls = new LinkedHashSet<>();
        Arrays.stream(menu.getPermission().split(","))
                .map(String::trim)
                .filter(CharSequenceUtil::isNotEmpty)
                .forEach(permissionUrls::add);

        if ("/goods-governance/goods-manage".equals(menu.getPath())) {
            permissionUrls.add("/manager/goods/goods*");
            permissionUrls.add("/manager/goods/goodsUnit*");
            permissionUrls.add("/manager/goods/categoryParameters*");
            permissionUrls.add("/manager/goods/freightTemplate/store*");
            permissionUrls.add("/manager/goods/category/allChildren*");
            permissionUrls.add("/manager/goods/brand/all*");
            permissionUrls.add("/manager/store/store/all*");
            permissionUrls.add("/common/common/upload/file*");
        }

        return new ArrayList<>(permissionUrls);
    }

}
