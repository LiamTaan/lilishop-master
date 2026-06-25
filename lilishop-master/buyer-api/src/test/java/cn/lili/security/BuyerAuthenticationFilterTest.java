package cn.lili.security;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.SecurityEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.SecretKeyUtil;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BuyerAuthenticationFilterTest {

    @Mock
    private Cache cache;

    @Test
    void shouldAuthenticateWhenAuthorizationHeaderContainsBearerToken() throws Exception {
        BuyerAuthenticationFilter filter = new BuyerAuthenticationFilter(authentication -> authentication, cache);
        AuthUser authUser = AuthUser.builder()
                .id("member-1")
                .username("qauser")
                .nickName("qauser")
                .role(UserEnums.MEMBER)
                .build();
        String token = Jwts.builder()
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                .subject(authUser.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SecretKeyUtil.generalKeyByDecoders())
                .compact();

        doReturn(true).when(cache).hasKey(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.MEMBER, authUser.getId()) + token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            Assertions.assertTrue(SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof AuthUser);
            AuthUser currentUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
            Assertions.assertEquals("member-1", currentUser.getId());
            return null;
        }).when(chain).doFilter(request, response);

        try {
            filter.doFilter(request, response, chain);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
