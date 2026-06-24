<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="提现设置"
    description="承接平台提现门槛、手续费和提现审核参数。"
    api-path="/manager/setting/setting"
    setting-key="WITHDRAWAL_SETTING"
    :fields="[
      { key: 'minPrice', label: '最低提现金额', type: 'number' },
      { key: 'fee', label: '提现手续费', type: 'number' },
      { key: 'type', label: '提现类型' },
      { key: 'apply', label: '是否需要审核', type: 'switch' },
      { key: 'wechatAppId', label: '微信提现 AppId' },
      { key: 'wechatAppIdSource', label: '微信提现 AppId 来源' }
    ]"
  />
</template>
