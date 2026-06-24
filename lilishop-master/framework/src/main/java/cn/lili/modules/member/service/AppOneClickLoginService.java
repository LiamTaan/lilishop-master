package cn.lili.modules.member.service;

/**
 * App 一键登录换号服务
 *
 * @author OpenAI
 */
public interface AppOneClickLoginService {

    /**
     * 通过一键登录临时凭证换取手机号
     *
     * @param loginToken 临时凭证
     * @return 手机号
     */
    String resolveMobile(String loginToken);
}
