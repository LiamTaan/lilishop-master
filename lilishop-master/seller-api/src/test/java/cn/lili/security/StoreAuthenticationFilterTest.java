package cn.lili.security;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.SecurityEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.SecretKeyUtil;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.StoreMenuRoleService;
import cn.lili.modules.member.token.StoreTokenGenerate;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import java.util.Date;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StoreAuthenticationFilterTest {

    @Mock
    private Cache cache;

    @Mock
    private StoreTokenGenerate storeTokenGenerate;

    @Mock
    private StoreMenuRoleService storeMenuRoleService;

    @Mock
    private ClerkService clerkService;

    @Test
    void shouldAuthenticateWhenAuthorizationHeaderContainsBearerToken() throws Exception {
        StoreAuthenticationFilter filter = new StoreAuthenticationFilter(
                authentication -> authentication,
                storeTokenGenerate,
                storeMenuRoleService,
                clerkService,
                cache
        );
        AuthUser authUser = AuthUser.builder()
                .id("store-member-1")
                .username("storeqa")
                .nickName("storeqa")
                .role(UserEnums.STORE)
                .isSuper(true)
                .storeId("store-1")
                .build();
        String token = Jwts.builder()
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                .subject(authUser.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SecretKeyUtil.generalKeyByDecoders())
                .compact();

        doReturn(true).when(cache).hasKey(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.STORE, authUser.getId()) + token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            Assertions.assertTrue(SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof AuthUser);
            AuthUser currentUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
            Assertions.assertEquals("store-member-1", currentUser.getId());
            Assertions.assertEquals("store-1", currentUser.getStoreId());
            return null;
        }).when(chain).doFilter(request, response);

        try {
            filter.doFilter(request, response, chain);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
