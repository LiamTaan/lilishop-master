package cn.lili.modules.sms.plugin;

import cn.lili.modules.sms.entity.enums.SmsEnum;

import java.util.List;
import java.util.Map;

/**
 * 短信插件接口
 *
 * @author Bulbasaur
 * @since 2023-02-16
 */
public interface SmsPlugin {

    /**
     * 插件名称
     */
    SmsEnum pluginName();

    /**
     * 短信发送
     *
     * @param mobile       接收手机号
     * @param param        参数
     * @param templateCode 模版code
     * @param signName     签名名称
     */
    void sendSmsCode(String signName, String mobile, Map<String, String> param, String templateCode);

    /**
     * 短信批量发送
     *
     * @param mobile       接收手机号
     * @param signName     签名
     * @param templateCode 模版code
     */
    void sendBatchSms(String signName, List<String> mobile, String templateCode);
}
