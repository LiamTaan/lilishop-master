<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="视频号配置"
    description="承接微信视频号接入参数、回调和渠道配置。"
    api-path="/manager/wxchannels/setting"
    setting-key="WX_CHANNELS"
    :fields="[
      { key: 'appId', label: '视频号 AppId' },
      { key: 'appSecret', label: '视频号 AppSecret', type: 'password' },
      { key: 'apiBase', label: '接口基础地址' },
      { key: 'tokenUrl', label: 'Token 获取地址' }
    ]"
  />
</template>
