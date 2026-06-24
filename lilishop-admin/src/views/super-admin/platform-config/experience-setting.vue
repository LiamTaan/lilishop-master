<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="经验值设置"
    description="承接经验值规则、成长体系与经验值说明配置。"
    api-path="/manager/setting/setting"
    setting-key="EXPERIENCE_SETTING"
    :fields="[
      { key: 'description', label: '经验值说明', type: 'textarea' },
      { key: 'items', label: '经验规则项', type: 'json', rows: 10, placeholder: '请输入 items JSON 数组' }
    ]"
  />
</template>
