package cn.lili.modules.member.service;

import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.enums.LoginIdentityCodeEnum;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import cn.lili.modules.member.entity.vo.LoginIdentitySelectionResultVO;
import cn.lili.modules.member.entity.vo.LoginSessionSnapshotVO;

import java.util.List;

/**
 * App 登录身份服务
 *
 * @author OpenAI
 */
public interface AppLoginIdentityService {

    /**
     * 解析会员当前可用身份
     *
     * @param member 会员信息
     * @return 身份列表
     */
    List<LoginIdentityOptionVO> resolveIdentityOptions(Member member);

    /**
     * 创建登录会话快照
     *
     * @param member 会员信息
     * @param mobile 手机号
     * @return 登录会话快照
     */
    LoginSessionSnapshotVO createLoginSession(Member member, String mobile, cn.lili.modules.member.entity.enums.LoginSessionChannelEnum channel);

    /**
     * 根据会话获取身份快照
     *
     * @param loginSessionToken 登录会话 token
     * @return 会话快照
     */
    LoginSessionSnapshotVO getLoginSessionSnapshot(String loginSessionToken);

    /**
     * 签发买家域 token
     *
     * @param loginSessionToken 登录会话 token
     * @param identityCode 身份编码
     * @return 登录结果
     */
    LoginIdentitySelectionResultVO issueMemberToken(String loginSessionToken, LoginIdentityCodeEnum identityCode);

    /**
     * 签发商家域 token
     *
     * @param loginSessionToken 登录会话 token
     * @param identityCode 身份编码
     * @return 登录结果
     */
    LoginIdentitySelectionResultVO issueStoreToken(String loginSessionToken, LoginIdentityCodeEnum identityCode);
}
