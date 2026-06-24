package cn.lili.modules.member.service;

import cn.lili.modules.member.entity.dto.LoginSessionDTO;
import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import cn.lili.modules.member.entity.vo.LoginSessionSnapshotVO;

import java.util.List;

/**
 * App 登录会话服务
 *
 * @author OpenAI
 */
public interface AppLoginSessionService {

    /**
     * 创建登录会话
     *
     * @param memberId 会员ID
     * @param mobile 手机号
     * @param channel 登录渠道
     * @param identityOptions 身份选项
     * @return 登录会话快照
     */
    LoginSessionSnapshotVO createSession(String memberId, String mobile, LoginSessionChannelEnum channel, List<LoginIdentityOptionVO> identityOptions);

    /**
     * 获取登录会话
     *
     * @param loginSessionToken 登录会话 token
     * @return 登录会话
     */
    LoginSessionDTO getSession(String loginSessionToken);

    /**
     * 删除登录会话
     *
     * @param loginSessionToken 登录会话 token
     */
    void removeSession(String loginSessionToken);
}
