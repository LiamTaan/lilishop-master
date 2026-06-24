<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="微信登录配置"
    description="承接微信联合登录参数和登录回调基础配置。"
    api-path="/manager/setting/setting"
    setting-key="WECHAT_CONNECT"
    :fields="[
      { key: 'wechatConnectSettingItems', label: '微信登录配置项', type: 'json', rows: 10, placeholder: '请输入微信登录配置项 JSON 数组' }
    ]"
  />
</template>
