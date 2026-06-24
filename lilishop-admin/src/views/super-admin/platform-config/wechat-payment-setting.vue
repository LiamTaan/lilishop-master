<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="微信支付配置"
    description="承接微信支付商户参数、证书和回调配置。"
    api-path="/manager/setting/setting"
    setting-key="WECHAT_PAYMENT"
    :fields="[
      { key: 'jsapiAppId', label: 'JSAPI AppId' },
      { key: 'nativeAppId', label: 'Native AppId' },
      { key: 'mpAppId', label: '小程序 AppId' },
      { key: 'h5AppId', label: 'H5 AppId' },
      { key: 'appAppId', label: 'APP AppId' },
      { key: 'mchId', label: '商户号' },
      { key: 'serialNumber', label: '商户证书序列号' },
      { key: 'apiclientKey', label: '私钥', type: 'password' },
      { key: 'publicId', label: '公钥ID' },
      { key: 'publicKey', label: '公钥', type: 'password' },
      { key: 'publicType', label: '验证方式' },
      { key: 'apiKey3', label: 'APIv3 密钥', type: 'password' },
      { key: 'callbackUrl', label: '回调地址' }
    ]"
  />
</template>
