<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="秒杀设置"
    description="承接秒杀活动时段、规则说明和业务基础配置。"
    api-path="/manager/setting/setting"
    setting-key="SECKILL_SETTING"
    :fields="[
      { key: 'hours', label: '秒杀时段' },
      { key: 'seckillRule', label: '秒杀规则', type: 'textarea', rows: 6 }
    ]"
  />
</template>
