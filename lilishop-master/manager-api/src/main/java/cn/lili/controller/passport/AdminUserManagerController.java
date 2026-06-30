package cn.lili.controller.passport;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.Token;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.SearchVO;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.permission.entity.dos.AdminUser;
import cn.lili.modules.permission.entity.dto.AdminUserProfileDTO;
import cn.lili.modules.permission.entity.dto.AdminUserDTO;
import cn.lili.modules.permission.entity.vo.AdminUserVO;
import cn.lili.modules.permission.service.AdminUserService;
import cn.lili.common.properties.SystemSettingProperties;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 管理员接口
 *
 * @author Chopper
 * @since 2020/11/16 10:57
 */
@Slf4j
@RestController
@RequestMapping("/manager/passport/user")
@Validated
public class AdminUserManagerController {
    @Autowired
    private AdminUserService adminUserService;
    /**
     * 客户
     */
    @Autowired
    private MemberService memberService;

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    private Cache cache;

    @Autowired
    private SystemSettingProperties systemSettingProperties;

    @PostMapping("/login")
    public ResultMessage<Token> login(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                      @NotNull(message = "密码不能为空") @RequestParam String password) {
        return ResultUtil.data(adminUserService.login(username, password));
    }

    @GetMapping("/sms/{mobile}")
    public ResultMessage<Object> sendLoginSmsCode(@RequestHeader String uuid,
                                                  @PathVariable String mobile) {
        smsUtil.sendSmsCode(mobile, VerificationEnums.LOGIN, uuid);
        return ResultUtil.success(ResultCode.VERIFICATION_SEND_SUCCESS);
    }

    @PostMapping("/mobile/login")
    public ResultMessage<Token> mobileLogin(@NotNull(message = "手机号不能为空") @RequestParam String mobile,
                                            @NotNull(message = "验证码不能为空") @RequestParam String code,
                                            @RequestHeader String uuid) {
        if (systemSettingProperties.getTestModel() || smsUtil.verifyCode(mobile, VerificationEnums.LOGIN, uuid, code)) {
            return ResultUtil.data(adminUserService.mobileLogin(mobile));
        }
        throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
    }

    @GetMapping("/reset/sms/{mobile}")
    public ResultMessage<Object> sendResetSmsCode(@RequestHeader String uuid,
                                                  @PathVariable String mobile) {
        smsUtil.sendSmsCode(mobile, VerificationEnums.FIND_USER, uuid);
        return ResultUtil.success(ResultCode.VERIFICATION_SEND_SUCCESS);
    }

    @PostMapping("/reset/verify")
    public ResultMessage<Object> verifyResetSms(@NotNull(message = "手机号不能为空") @RequestParam String mobile,
                                                @NotNull(message = "验证码不能为空") @RequestParam String code,
                                                @RequestHeader String uuid) {
        if (!smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        AdminUser adminUser = adminUserService.findByMobile(mobile);
        if (adminUser == null) {
            throw new ServiceException(ResultCode.USER_PHONE_NOT_EXIST);
        }
        cache.put(CachePrefix.MOBILE_VALIDATE.getPrefix("manager_reset_" + uuid + "_" + mobile), 1, 300L, TimeUnit.SECONDS);
        return ResultUtil.success();
    }

    @PostMapping("/reset/password")
    public ResultMessage<Object> resetPasswordByMobile(@NotNull(message = "手机号不能为空") @RequestParam String mobile,
                                                       @NotNull(message = "密码不能为空") @RequestParam String password,
                                                       @RequestHeader String uuid) {
        adminUserService.resetPasswordByMobile(uuid, mobile, password);
        return ResultUtil.success(ResultCode.USER_EDIT_SUCCESS);
    }

    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        this.memberService.logout(UserEnums.MANAGER);
        return ResultUtil.success();
    }

    @GetMapping("/refresh/{refreshToken}")
    public ResultMessage<Object> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        return ResultUtil.data(this.adminUserService.refreshToken(refreshToken));
    }


    @GetMapping("/info")
    public ResultMessage<AdminUser> getUserInfo() {
        AuthUser tokenUser = UserContext.getCurrentUser();
        if (tokenUser != null) {
            AdminUser adminUser = adminUserService.findByUsername(tokenUser.getUsername());
            adminUser.setPassword(null);
            return ResultUtil.data(adminUser);
        }
        throw new ServiceException(ResultCode.USER_NOT_LOGIN);
    }

    @PutMapping("/edit")
    public ResultMessage<Object> editOwner(@Valid @RequestBody AdminUserProfileDTO adminUserProfileDTO) {
        adminUserService.updateOwnerProfile(adminUserProfileDTO);
        return ResultUtil.success(ResultCode.USER_EDIT_SUCCESS);
    }

    @PutMapping("/admin/edit")
    @DemoSite
    public ResultMessage<Object> edit(@Valid AdminUser adminUser,
                                      @RequestParam(required = false) List<String> roles) {
        if (!adminUserService.updateAdminUser(adminUser, roles)) {
            throw new ServiceException(ResultCode.USER_EDIT_ERROR);
        }
        return ResultUtil.success(ResultCode.USER_EDIT_SUCCESS);
    }

    /**
     * 修改密码
     *
     * @param password
     * @param newPassword
     * @return
     */
    @PutMapping("/editPassword")
    @DemoSite
    public ResultMessage<Object> editPassword(String password, String newPassword) {
        adminUserService.editPassword(password, newPassword);
        return ResultUtil.success(ResultCode.USER_EDIT_SUCCESS);
    }

    @PostMapping("/resetPassword/{ids}")
    @DemoSite
    public ResultMessage<Object> resetPassword(@PathVariable List<String> ids) {
        adminUserService.resetPassword(ids);
        return ResultUtil.success(ResultCode.USER_EDIT_SUCCESS);
    }

    @GetMapping("/getByCondition")
    public ResultMessage<IPage<AdminUserVO>> getByCondition(AdminUserDTO user,
                                                            SearchVO searchVo,
                                                            PageVO pageVo) {
        IPage<AdminUserVO> page = adminUserService.adminUserPage(user, searchVo, pageVo);

        return ResultUtil.data(page);
    }


    @PostMapping("/register")
    public ResultMessage<Object> register(@Valid AdminUserDTO adminUser,
                                          @RequestParam(required = false) List<String> roles) {
        int rolesMaxSize = 10;
        try {
            if (roles != null && roles.size() >= rolesMaxSize) {
                throw new ServiceException(ResultCode.PERMISSION_BEYOND_TEN);
            }
            adminUserService.saveAdminUser(adminUser, roles);
            return ResultUtil.success();
        } catch (Exception e) {
            log.error("添加用户错误", e);
            return ResultUtil.error(ResultCode.USER_ADD_ERROR);
        }
    }

    @PutMapping("/enable/{userId}")
    @DemoSite
 public ResultMessage<Object> disable(@PathVariable String userId, Boolean status) {
        AdminUser user = adminUserService.getById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        user.setStatus(status);
        adminUserService.updateById(user);

        //登出用户
        if (Boolean.FALSE.equals(status)) {
            List<String> userIds = new ArrayList<>();
            userIds.add(userId);
            adminUserService.logout(userIds);
        }

        return ResultUtil.success();
    }

    @DeleteMapping("/{ids}")
    @DemoSite
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        adminUserService.deleteCompletely(ids);
        return ResultUtil.success();
    }

}
