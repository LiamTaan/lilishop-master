package cn.lili.controller.common;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dto.AppLoginOneClickDTO;
import cn.lili.modules.member.entity.dto.AppLoginSmsDTO;
import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginSessionSnapshotVO;
import cn.lili.modules.member.service.AppLoginIdentityService;
import cn.lili.modules.member.service.AppOneClickLoginService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 统一认证接口
 *
 * @author OpenAI
 */
@RestController
@Tag(name = "App统一认证接口")
@RequestMapping("/common/auth/login")
public class AppAuthController {

    @Autowired
    private SmsUtil smsUtil;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AppLoginIdentityService appLoginIdentityService;
    @Autowired
    private AppOneClickLoginService appOneClickLoginService;

    @PostMapping("/sms")
    @Operation(
            summary = "App-短信认证登录",
            description = "校验短信验证码后创建 App 登录会话，不直接签发业务 token。成功后返回 `loginSessionToken` 和可选身份列表，前端需继续调用身份选择接口换取最终登录 token。若手机号未注册，系统会自动创建会员账号。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "认证成功，返回登录会话和身份列表",
                    content = @Content(schema = @Schema(implementation = LoginSessionSnapshotVO.class))),
            @ApiResponse(responseCode = "200", description = "验证码错误或已失效，业务码 `80210`"),
            @ApiResponse(responseCode = "200", description = "请求参数缺失或格式错误，业务码 `4002`")
    })
    public ResultMessage<LoginSessionSnapshotVO> smsLogin(
            @Valid @RequestBody AppLoginSmsDTO dto,
            @Parameter(
                    name = "uuid",
                    description = "短信验证码会话标识。需与发送验证码接口返回或前端持有的 `uuid` 保持一致，用于校验本次登录验证码。",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "5d2c6c4c-1c6d-4c35-8c5e-0f9b7aa12abc"
            )
            @RequestHeader String uuid) {
        if (!smsUtil.verifyCode(dto.getMobile(), VerificationEnums.LOGIN, uuid, dto.getCode())) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        Member member = memberService.ensureMemberByMobile(dto.getMobile());
        return ResultUtil.data(appLoginIdentityService.createLoginSession(member, dto.getMobile(), LoginSessionChannelEnum.SMS));
    }

    @PostMapping("/one-click")
    @Operation(
            summary = "App-本机号码一键登录认证",
            description = "使用运营商一键登录凭证换取手机号并创建 App 登录会话，不直接签发业务 token。成功后返回 `loginSessionToken` 和可选身份列表，前端需继续调用身份选择接口换取最终登录 token。若换取到的手机号未注册，系统会自动创建会员账号。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "认证成功，返回登录会话和身份列表",
                    content = @Content(schema = @Schema(implementation = LoginSessionSnapshotVO.class))),
            @ApiResponse(responseCode = "200", description = "当前短信配置不支持一键登录，业务码 `20056`"),
            @ApiResponse(responseCode = "200", description = "一键登录凭证无效、已过期或服务商换号失败，业务码 `20055`"),
            @ApiResponse(responseCode = "200", description = "请求参数缺失或格式错误，业务码 `4002`")
    })
    public ResultMessage<LoginSessionSnapshotVO> oneClickLogin(@Valid @RequestBody AppLoginOneClickDTO dto) {
        String mobile = appOneClickLoginService.resolveMobile(dto.getLoginToken());
        Member member = memberService.ensureMemberByMobile(mobile);
        return ResultUtil.data(appLoginIdentityService.createLoginSession(member, mobile, LoginSessionChannelEnum.ONE_CLICK));
    }

    @GetMapping("/identities/{loginSessionToken}")
    @Operation(
            summary = "App-获取登录会话身份列表",
            description = "根据 `loginSessionToken` 查询当前登录会话下可进入的身份列表。登录会话默认有效期 10 分钟，过期后需要重新发起短信认证或一键登录认证。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回最新身份状态快照",
                    content = @Content(schema = @Schema(implementation = LoginSessionSnapshotVO.class))),
            @ApiResponse(responseCode = "200", description = "登录会话无效，业务码 `20045`"),
            @ApiResponse(responseCode = "200", description = "登录会话已过期，业务码 `20046`")
    })
    public ResultMessage<LoginSessionSnapshotVO> identities(
            @Parameter(
                    description = "短信认证或一键登录成功后返回的登录会话 token。",
                    required = true,
                    example = "LOGIN_SESSION196877654321"
            )
            @PathVariable String loginSessionToken) {
        return ResultUtil.data(appLoginIdentityService.getLoginSessionSnapshot(loginSessionToken));
    }
}
