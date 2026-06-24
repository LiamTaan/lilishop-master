package cn.lili.controller.wallet;

import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.SecurityEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.SecretKeyUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
import cn.lili.modules.wallet.service.MemberWalletService;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberWalletBuyerControllerTest {

    @InjectMocks
    private MemberWalletBuyerController memberWalletBuyerController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberWalletService memberWalletService;

    @Mock
    private SmsUtil smsUtil;

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void setPasswordShouldVerifySmsCodeAndReturnSuccess() {
        setCurrentUser("member-1", "qauser");
        Member member = new Member();
        member.setId("member-1");
        member.setUsername("qauser");
        member.setMobile("13900000000");

        doReturn(member).when(memberService).getById("member-1");
        doReturn(true).when(smsUtil).verifyCode("13900000000", VerificationEnums.WALLET_PASSWORD, "uuid-1", "111111");
        doNothing().when(memberWalletService).setMemberWalletPassword(member, "e10adc3949ba59abbe56e057f20f883e");

        ResultMessage<Object> result = memberWalletBuyerController.setPassword("e10adc3949ba59abbe56e057f20f883e", "111111", "uuid-1");

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(200, result.getCode());
        verify(smsUtil).verifyCode("13900000000", VerificationEnums.WALLET_PASSWORD, "uuid-1", "111111");
        verify(memberWalletService).setMemberWalletPassword(member, "e10adc3949ba59abbe56e057f20f883e");
    }

    private void setCurrentUser(String memberId, String username) {
        AuthUser authUser = AuthUser.builder()
                .id(memberId)
                .username(username)
                .nickName(username)
                .role(UserEnums.MEMBER)
                .build();
        String token = Jwts.builder()
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SecretKeyUtil.generalKeyByDecoders())
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityEnum.HEADER_TOKEN.getValue(), token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
