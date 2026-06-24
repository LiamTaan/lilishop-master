<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="QQ登录配置"
    description="承接 QQ 联合登录参数、回调地址和开关配置。"
    api-path="/manager/setting/setting"
    setting-key="QQ_CONNECT"
    :fields="[
      { key: 'qqConnectSettingItemList', label: 'QQ 登录配置项', type: 'json', rows: 10, placeholder: '请输入 QQ 登录配置项 JSON 数组' }
    ]"
  />
</template>
