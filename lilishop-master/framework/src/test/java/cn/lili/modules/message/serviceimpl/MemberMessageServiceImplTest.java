package cn.lili.modules.message.serviceimpl;

import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.SecurityEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.SecretKeyUtil;
import cn.lili.modules.message.entity.dos.MemberMessage;
import cn.lili.modules.message.entity.enums.MessageStatusEnum;
import cn.lili.modules.message.entity.vos.MemberMessageQueryVO;
import cn.lili.modules.message.mapper.MemberMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemberMessageServiceImplTest {

    @Spy
    @InjectMocks
    private MemberMessageServiceImpl memberMessageService;

    @Mock
    private MemberMessageMapper memberMessageMapper;

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getPageShouldExcludeRemovedMessagesByDefault() {
        ReflectionTestUtils.setField(memberMessageService, "baseMapper", memberMessageMapper);
        MemberMessageQueryVO queryVO = new MemberMessageQueryVO();

        doAnswer(invocation -> {
            QueryWrapper<MemberMessage> wrapper = invocation.getArgument(1);
            Assertions.assertTrue(wrapper.getSqlSegment().contains("status <>"));
            Assertions.assertTrue(wrapper.getParamNameValuePairs().containsValue(MessageStatusEnum.ALREADY_REMOVE.name()));
            return new Page<MemberMessage>();
        }).when(memberMessageService).page(any(IPage.class), any(QueryWrapper.class));

        memberMessageService.getPage(queryVO, new cn.lili.common.vo.PageVO());
    }

    @Test
    void editStatusShouldThrowWhenMessageDoesNotBelongToCurrentMember() {
        ReflectionTestUtils.setField(memberMessageService, "baseMapper", memberMessageMapper);
        setCurrentUser("current-member");

        MemberMessage message = new MemberMessage();
        message.setId("msg-1");
        message.setMemberId("other-member");
        doReturn(message).when(memberMessageService).getById("msg-1");

        Assertions.assertThrows(ServiceException.class,
                () -> memberMessageService.editStatus(MessageStatusEnum.ALREADY_READY.name(), "msg-1"));
    }

    private void setCurrentUser(String memberId) {
        AuthUser authUser = AuthUser.builder()
                .id(memberId)
                .username("qa-user")
                .nickName("qa-user")
                .role(UserEnums.MEMBER)
                .build();
        String token = Jwts.builder()
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                .subject("qa-user")
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SecretKeyUtil.generalKeyByDecoders())
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityEnum.HEADER_TOKEN.getValue(), token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
