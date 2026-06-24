package cn.lili.modules.member.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.SnowFlake;
import cn.lili.modules.member.entity.dto.LoginSessionDTO;
import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import cn.lili.modules.member.entity.vo.LoginSessionSnapshotVO;
import cn.lili.modules.member.service.AppLoginSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * App 登录会话服务实现
 *
 * @author OpenAI
 */
@Service
public class AppLoginSessionServiceImpl implements AppLoginSessionService {

    private static final long LOGIN_SESSION_TTL_MINUTES = 10L;

    @Autowired
    private Cache<Object> cache;

    @Override
    public LoginSessionSnapshotVO createSession(String memberId, String mobile, LoginSessionChannelEnum channel, List<LoginIdentityOptionVO> identityOptions) {
        String token = CachePrefix.LOGIN_SESSION.name() + SnowFlake.getIdStr();
        LoginSessionDTO session = new LoginSessionDTO();
        session.setToken(token);
        session.setMemberId(memberId);
        session.setMobile(mobile);
        session.setChannel(channel);
        session.setIdentityOptions(identityOptions);
        session.setExpireAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(LOGIN_SESSION_TTL_MINUTES));
        cache.put(cacheKey(token), session, LOGIN_SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        LoginSessionSnapshotVO snapshot = new LoginSessionSnapshotVO();
        snapshot.setLoginSessionToken(token);
        snapshot.setMemberId(memberId);
        snapshot.setMobile(mobile);
        snapshot.setChannel(channel);
        snapshot.setIdentityOptions(identityOptions);
        return snapshot;
    }

    @Override
    public LoginSessionDTO getSession(String loginSessionToken) {
        Object cached = cache.get(cacheKey(loginSessionToken));
        if (cached == null) {
            throw new ServiceException(ResultCode.LOGIN_SESSION_EXPIRED);
        }
        if (!(cached instanceof LoginSessionDTO session)) {
            throw new ServiceException(ResultCode.LOGIN_SESSION_INVALID);
        }
        if (session.getExpireAt() != null && session.getExpireAt() < System.currentTimeMillis()) {
            cache.remove(cacheKey(loginSessionToken));
            throw new ServiceException(ResultCode.LOGIN_SESSION_EXPIRED);
        }
        return session;
    }

    @Override
    public void removeSession(String loginSessionToken) {
        cache.remove(cacheKey(loginSessionToken));
    }

    private String cacheKey(String loginSessionToken) {
        return CachePrefix.LOGIN_SESSION.getPrefix(loginSessionToken);
    }
}
