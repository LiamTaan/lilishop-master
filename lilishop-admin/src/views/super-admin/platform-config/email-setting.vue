<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="邮件配置"
    description="承接平台邮件服务、发件账号与发送参数配置。"
    api-path="/manager/setting/setting"
    setting-key="EMAIL_SETTING"
    :fields="[
      { key: 'host', label: 'SMTP Host' },
      { key: 'username', label: '发件账号' },
      { key: 'password', label: '邮箱授权码', type: 'password' }
    ]"
  />
</template>
