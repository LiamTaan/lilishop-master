<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="登录设置"
    description="承接第三方登录基础开关和登录策略配置。"
    api-path="/manager/setting/setting"
    setting-key="CONNECT_SETTING"
    :fields="[
      { key: 'pc', label: '买家 PC 端域名' },
      { key: 'wap', label: '买家 WAP 端域名' },
      { key: 'callbackUrl', label: '统一回调域名' }
    ]"
  />
</template>
