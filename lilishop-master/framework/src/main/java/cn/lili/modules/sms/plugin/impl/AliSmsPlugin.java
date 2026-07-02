package cn.lili.modules.sms.plugin.impl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.sms.entity.enums.SmsEnum;
import cn.lili.modules.sms.plugin.SmsPlugin;
import cn.lili.modules.system.entity.dto.SmsSetting;
import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 阿里云短信插件
 *
 * @author Bulbasaur
 * @since 2023-02-16
 */
@Slf4j
public class AliSmsPlugin implements SmsPlugin {

    private SmsSetting smsSetting;

    public AliSmsPlugin(SmsSetting smsSetting) {
        this.smsSetting = smsSetting;
    }

    @Override
    public SmsEnum pluginName() {
        return SmsEnum.ALI;
    }

    @Override
    public void sendSmsCode(String signName, String mobile, Map<String, String> param, String templateCode) {
        com.aliyun.dysmsapi20170525.Client client = this.createClient();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setPhoneNumbers(mobile)
                .setTemplateCode(templateCode)
                .setTemplateParam(JSON.toJSONString(param));
        try {
            SendSmsResponse response = client.sendSms(sendSmsRequest);
            if (!("OK").equals(response.getBody().getCode())) {
                throw new ServiceException(response.getBody().getMessage());
            }
        } catch (Exception e) {
            log.error("发送短信错误", e);
        }
    }


    @Override
    public void sendBatchSms(String signName, List<String> mobile, String templateCode) {

        com.aliyun.dysmsapi20170525.Client client = this.createClient();

        List<String> sign = new ArrayList<String>();

        sign.addAll(mobile);
        sign.replaceAll(e -> signName);

        //手机号拆成多个小组进行发送
        List<List<String>> mobileList = new ArrayList<>();

        //签名名称多个小组
        List<List<String>> signNameList = new ArrayList<>();

        //循环分组
        for (int i = 0; i < (mobile.size() / 100 + (mobile.size() % 100 == 0 ? 0 : 1)); i++) {
            int endPoint = Math.min((100 + (i * 100)), mobile.size());
            mobileList.add(mobile.subList((i * 100), endPoint));
            signNameList.add(sign.subList((i * 100), endPoint));
        }

//       //发送短信
        for (int i = 0; i < mobileList.size(); i++) {
            SendBatchSmsRequest sendBatchSmsRequest = new SendBatchSmsRequest()
                    .setPhoneNumberJson(JSON.toJSONString(mobileList.get(i)))
                    .setSignNameJson(JSON.toJSONString(signNameList.get(i)))
                    .setTemplateCode(templateCode);
            try {
                client.sendBatchSms(sendBatchSmsRequest);
            } catch (Exception e) {
                log.error("批量发送短信错误", e);
            }
        }

    }


    /**
     * 初始化账号Client
     *
     * @return Client 短信操作
     */
    public com.aliyun.dysmsapi20170525.Client createClient() {
        try {
            if (smsSetting == null) {
                throw new ServiceException(ResultCode.ALI_SMS_SETTING_ERROR);
            }
            Config config = new Config();
            //您的AccessKey ID
            config.accessKeyId = smsSetting.getAccessKeyId();
            //您的AccessKey Secret
            config.accessKeySecret = smsSetting.getAccessSecret();
            //访问的域名
            config.endpoint = "dysmsapi.aliyuncs.com";
            return new com.aliyun.dysmsapi20170525.Client(config);
        } catch (Exception e) {
            log.error("短信初始化错误", e);
        }
        return null;
    }

}
