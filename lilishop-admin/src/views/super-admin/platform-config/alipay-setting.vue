<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="支付宝配置"
    description="承接支付宝支付参数、应用信息和回调配置。"
    api-path="/manager/setting/setting"
    setting-key="ALIPAY_PAYMENT"
    :fields="[
      { key: 'appId', label: '应用 AppId' },
      { key: 'privateKey', label: '应用私钥', type: 'password' },
      { key: 'certPath', label: '应用证书路径', type: 'password' },
      { key: 'alipayPublicCertPath', label: '支付宝公钥证书路径', type: 'password' },
      { key: 'rootCertPath', label: '支付宝根证书路径', type: 'password' },
      { key: 'callbackUrl', label: '回调地址' }
    ]"
  />
</template>
