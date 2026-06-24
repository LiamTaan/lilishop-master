package cn.lili.modules.permission.service;


import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.SearchVO;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.Token;
import cn.lili.modules.permission.entity.dos.AdminUser;
import cn.lili.modules.permission.entity.dto.AdminUserProfileDTO;
import cn.lili.modules.permission.entity.dto.AdminUserDTO;
import cn.lili.modules.permission.entity.vo.AdminUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

/**
 * 用户业务层
 *
 * @author Chopper
 * @since 2020/11/17 3:42 下午
 */
@CacheConfig(cacheNames = "{adminuser}")
public interface AdminUserService extends IService<AdminUser> {


    /**
     * 获取管理员分页
     *
     * @param initPage
     * @param initWrapper
     * @return
     */
    IPage<AdminUserVO> adminUserPage(Page initPage, QueryWrapper<AdminUser> initWrapper);

    /**
     * 多条件分页获取用户列表（由 service 内部构造查询条件）
     *
     * @param adminUserDTO 查询参数
     * @param searchVO 查询条件
     * @param pageVO 分页参数
     * @return 分页结果
     */
    IPage<AdminUserVO> adminUserPage(AdminUserDTO adminUserDTO, SearchVO searchVO, PageVO pageVO);

    /**
     * 通过用户名获取用户
     *
     * @param username
     * @return
     */
    AdminUser findByUsername(String username);

    /**
     * 通过手机号获取管理员
     *
     * @param mobile 手机号
     * @return 管理员
     */
    AdminUser findByMobile(String mobile);

    /**
     * 修改当前登录管理员资料
     *
     * @param profileDTO 资料参数
     */
    void updateOwnerProfile(AdminUserProfileDTO profileDTO);


    /**
     * 更新管理员
     *
     * @param adminUser
     * @param roles
     * @return
     */
    boolean updateAdminUser(AdminUser adminUser, List<String> roles);


    /**
     * 修改管理员密码
     *
     * @param password
     * @param newPassword
     */
    void editPassword(String password, String newPassword);

    /**
     * 重置密码
     *
     * @param ids id集合
     */
    void resetPassword(List<String> ids);

    /**
     * 新增管理员
     *
     * @param adminUser
     * @param roles
     */
    void saveAdminUser(AdminUserDTO adminUser, List<String> roles);

    /**
     * 彻底删除
     *
     * @param ids
     */
    void deleteCompletely(List<String> ids);


    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    Token login(String username, String password);

    /**
     * 手机号验证码登录
     *
     * @param mobile 手机号
     * @return token
     */
    Token mobileLogin(String mobile);

    /**
     * 管理员手机号找回密码
     *
     * @param uuid uuid
     * @param mobile 手机号
     * @param password 新密码
     */
    void resetPasswordByMobile(String uuid, String mobile, String password);

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return token
     */
    Token refreshToken(String refreshToken);

    /**
     * 登出
     *
     * @param userEnums token角色类型
     */
    void logout(UserEnums userEnums);

    /**
     * 登出
     *
     * @param adminUserIds 用户id
     */
    void logout(List<String> adminUserIds);

}
