<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="短信配置"
    description="承接短信通道、短信签名和发送参数治理。"
    api-path="/manager/setting/setting"
    setting-key="SMS_SETTING"
    :fields="[
      { key: 'type', label: '短信通道类型' },
      { key: 'isTestModel', label: '测试模式', type: 'switch' },
      { key: 'loginTemplateCode', label: '登录模板CODE' },
      { key: 'registerTemplateCode', label: '注册模板CODE' },
      { key: 'findPasswordTemplateCode', label: '找回密码模板CODE' },
      { key: 'walletPasswordTemplateCode', label: '钱包密码模板CODE' },
      { key: 'payPasswordTemplateCode', label: '支付密码模板CODE' },
      { key: 'accessKeyId', label: '阿里云 AccessKeyId', type: 'password' },
      { key: 'accessSecret', label: '阿里云 AccessSecret', type: 'password' },
      { key: 'signName', label: '阿里云签名' },
      { key: 'tencentSecretId', label: '腾讯云 SecretId', type: 'password' },
      { key: 'tencentSecretKey', label: '腾讯云 SecretKey', type: 'password' },
      { key: 'tencentSdkAppId', label: '腾讯云 SDK AppId' },
      { key: 'tencentSignName', label: '腾讯云签名' },
      { key: 'huaweiAppKey', label: '华为 AppKey', type: 'password' },
      { key: 'huaweiAppSecret', label: '华为 AppSecret', type: 'password' },
      { key: 'huaweiSender', label: '华为通道号' },
      { key: 'huaweiSignature', label: '华为签名' }
    ]"
  />
</template>
