package cn.lili.controller.passport;


import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.LoginIdentitySelectDTO;
import cn.lili.modules.member.entity.vo.LoginIdentitySelectionResultVO;
import cn.lili.modules.member.service.AppLoginIdentityService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
import cn.lili.modules.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 店铺端,商家登录接口
 *
 * @author Chopper
 * @since 2020/12/22 15:02
 */

@RestController
@Tag(name = "店铺端,商家登录接口 ")
@RequestMapping("/store/passport/login")
public class StorePassportController {

    /**
     * 客户
     */
    @Autowired
    private MemberService memberService;

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    private VerificationService verificationService;
    @Autowired
    private AppLoginIdentityService appLoginIdentityService;

    @Operation(description = "登录接口")
    @Parameter(name = "username", description = "用户名", required = true)
    @Parameter(name = "password", description = "密码", required = true)
    @PostMapping("/userLogin")
    public ResultMessage<Object> userLogin(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                           @NotNull(message = "密码不能为空") @RequestParam String password, @RequestHeader String uuid) {
        if (verificationService.check(uuid, VerificationEnums.LOGIN)) {
            return ResultUtil.data(this.memberService.usernameStoreLogin(username, password));
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_ERROR);
        }
    }

    @Operation(description = "短信登录接口")
    @Parameter(name = "mobile", description = "手机号", required = true)
    @Parameter(name = "code", description = "验证码", required = true)
    @PostMapping("/smsLogin")
    public ResultMessage<Object> smsLogin(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                          @NotNull(message = "验证码为空") @RequestParam String code,
                                          @RequestHeader String uuid) {
        if (smsUtil.verifyCode(mobile, VerificationEnums.LOGIN, uuid, code)) {
            return ResultUtil.data(memberService.mobilePhoneStoreLogin(mobile));
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
    }

    @Operation(
            summary = "App-选择供货商身份并签发token",
            description = "使用 `loginSessionToken` 选择供货商身份并签发最终登录 token。当前接口仅支持 `SUPPLIER` 身份；如果账号尚未开通供货商身份、正在审核、审核未通过或账号已禁用，会返回对应业务码。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "签发成功，返回最终 token、会员信息和最新身份状态汇总",
                    content = @Content(schema = @Schema(implementation = LoginIdentitySelectionResultVO.class))),
            @ApiResponse(responseCode = "200", description = "登录会话无效，业务码 `20045`"),
            @ApiResponse(responseCode = "200", description = "登录会话已过期，业务码 `20046`"),
            @ApiResponse(responseCode = "200", description = "当前接口不支持所选身份，业务码 `20047`"),
            @ApiResponse(responseCode = "200", description = "供货商资格审核中，业务码 `20051`"),
            @ApiResponse(responseCode = "200", description = "供货商资格审核未通过，业务码 `20052`"),
            @ApiResponse(responseCode = "200", description = "供货商账号已禁用，业务码 `20053`"),
            @ApiResponse(responseCode = "200", description = "供货商账号不存在或未开通，业务码 `20054`"),
            @ApiResponse(responseCode = "200", description = "身份暂不可进入或请求参数错误，业务码 `20048` / `4002`")
    })
    @PostMapping("/selectIdentity")
    public ResultMessage<LoginIdentitySelectionResultVO> selectIdentity(@RequestBody @jakarta.validation.Valid LoginIdentitySelectDTO dto) {
        return ResultUtil.data(appLoginIdentityService.issueStoreToken(dto.getLoginSessionToken(), dto.getIdentityCode()));
    }

    @Operation(description = "注销接口")
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        this.memberService.logout(UserEnums.STORE);
        return ResultUtil.success();
    }

    @Operation(description = "通过短信重置密码")
    @Parameter(name = "mobile", description = "手机号", required = true)
    @Parameter(name = "code", description = "验证码", required = true)
    @PostMapping("/resetByMobile")
    public ResultMessage<Member> resetByMobile(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                               @NotNull(message = "验证码为空") @RequestParam String code,
                                               @RequestHeader String uuid) {
        //校验短信验证码是否正确
        if (smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
            //校验是否通过手机号可获取客户,存在则将客户信息存入缓存，有效时间3分钟
            memberService.findByMobile(uuid, mobile);
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
    }
    @Operation(description = "修改密码")
    @Parameter(name = "password", description = "密码", required = true)
    @Parameter(name = "newPassword", description = "新密码", required = true)
    @PostMapping("/resetPassword")
    public ResultMessage<Object> resetByMobile(@NotNull(message = "密码为空") @RequestParam String password, @RequestHeader String uuid) {
        return ResultUtil.data(memberService.resetByMobile(uuid, password));
    }

    @Operation(description = "修改密码")
    @Parameter(name = "password", description = "旧密码", required = true)
    @Parameter(name = "newPassword", description = "新密码", required = true)
    @PostMapping("/modifyPass")
    public ResultMessage<Member> modifyPass(@NotNull(message = "旧密码不能为空") @RequestParam String password,
                                            @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        AuthUser tokenUser = UserContext.getCurrentUser();
        if (tokenUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(memberService.modifyPass(password, newPassword));
    }

    @Operation(description = "刷新token")
    @Parameter(name = "refreshToken", description = "刷新token", required = true)
    @GetMapping("/refresh/{refreshToken}")
    public ResultMessage<Object> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        return ResultUtil.data(this.memberService.refreshStoreToken(refreshToken));
    }
}
