<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="支付配置"
    description="承接平台支付开关、支付方式和支付参数配置。"
    api-path="/manager/setting/setting"
    setting-key="PAYMENT_SUPPORT"
    :fields="[
      { key: 'paymentSupportItems', label: '支付支持项', type: 'json', rows: 10, placeholder: '请输入支付支持配置 JSON 数组' }
    ]"
  />
</template>
