<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="银联支付配置"
    description="承接银联支付接入参数、证书和渠道配置。"
    api-path="/manager/setting/setting"
    setting-key="UNIONPAY_PAYMENT"
    :fields="[
      { key: 'unionPayMachId', label: '商户号' },
      { key: 'unionPayKey', label: '密钥', type: 'password' },
      { key: 'unionPayServerUrl', label: '请求地址' },
      { key: 'unionPayDomain', label: '交易请求地址' },
      { key: 'unionPayAppId', label: '应用ID' }
    ]"
  />
</template>
