<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="分销设置"
    description="承接分销业务开关、分销层级和分销规则基础配置。"
    api-path="/manager/setting/setting"
    setting-key="DISTRIBUTION_SETTING"
    :fields="[
      { key: 'isOpen', label: '分销开关', type: 'switch' },
      { key: 'distributionDay', label: '分销关系绑定天数', type: 'number' }
    ]"
  />
</template>
