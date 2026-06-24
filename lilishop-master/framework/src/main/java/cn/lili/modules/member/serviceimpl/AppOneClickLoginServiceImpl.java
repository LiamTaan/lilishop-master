package cn.lili.modules.member.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.member.service.AppOneClickLoginService;
import cn.lili.modules.sms.entity.enums.SmsEnum;
import cn.lili.modules.system.entity.dos.Setting;
import cn.lili.modules.system.entity.dto.SmsSetting;
import cn.lili.modules.system.entity.enums.SettingEnum;
import cn.lili.modules.system.service.SettingService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * App 一键登录换号服务实现
 *
 * @author OpenAI
 */
@Service
public class AppOneClickLoginServiceImpl implements AppOneClickLoginService {

    private static final String DYPNS_DOMAIN = "dypnsapi.aliyuncs.com";
    private static final String DYPNS_VERSION = "2017-05-25";
    private static final String DYPNS_ACTION = "GetMobile";

    @Autowired
    private SettingService settingService;

    @Override
    public String resolveMobile(String loginToken) {
        SmsSetting smsSetting = getAliSmsSetting();
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsSetting.getAccessKeyId(), smsSetting.getAccessSecret());
            DefaultAcsClient client = new DefaultAcsClient(profile);
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain(DYPNS_DOMAIN);
            request.setSysVersion(DYPNS_VERSION);
            request.setSysAction(DYPNS_ACTION);
            request.putQueryParameter("AccessToken", loginToken);
            CommonResponse response = client.getCommonResponse(request);
            JSONObject jsonObject = JSON.parseObject(response.getData());
            String code = jsonObject.getString("Code");
            if (!"OK".equalsIgnoreCase(code)) {
                throw new ServiceException(ResultCode.ONE_CLICK_LOGIN_ERROR, jsonObject.getString("Message"));
            }
            JSONObject result = jsonObject.getJSONObject("GetMobileResultDTO");
            if (result == null || CharSequenceUtil.isBlank(result.getString("Mobile"))) {
                throw new ServiceException(ResultCode.ONE_CLICK_LOGIN_ERROR);
            }
            return result.getString("Mobile");
        } catch (ServiceException serviceException) {
            throw serviceException;
        } catch (Exception exception) {
            throw new ServiceException(ResultCode.ONE_CLICK_LOGIN_ERROR, exception.getMessage());
        }
    }

    private SmsSetting getAliSmsSetting() {
        Setting setting = settingService.get(SettingEnum.SMS_SETTING.name());
        if (setting == null || CharSequenceUtil.isBlank(setting.getSettingValue())) {
            throw new ServiceException(ResultCode.ONE_CLICK_LOGIN_UNSUPPORTED);
        }
        SmsSetting smsSetting = JSON.parseObject(setting.getSettingValue(), SmsSetting.class);
        if (smsSetting == null
                || CharSequenceUtil.isBlank(smsSetting.getType())
                || !SmsEnum.ALI.name().equalsIgnoreCase(smsSetting.getType())
                || CharSequenceUtil.isBlank(smsSetting.getAccessKeyId())
                || CharSequenceUtil.isBlank(smsSetting.getAccessSecret())) {
            throw new ServiceException(ResultCode.ONE_CLICK_LOGIN_UNSUPPORTED);
        }
        return smsSetting;
    }
}
