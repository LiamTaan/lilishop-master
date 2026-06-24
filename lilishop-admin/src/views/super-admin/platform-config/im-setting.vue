<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="IM配置"
    description="承接客服 IM 基础开关、接入参数和提示信息配置。"
    api-path="/manager/setting/setting"
    setting-key="IM_SETTING"
    :fields="[
      { key: 'httpUrl', label: 'IM 平台地址' }
    ]"
  />
</template>
