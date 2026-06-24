<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="热词设置"
    description="承接平台热词、搜索词推荐和热词说明配置。"
    api-path="/manager/setting/setting"
    setting-key="HOT_WORDS"
    :fields="[
      { key: 'saveNum', label: '每日保存数量', type: 'number' },
      { key: 'hotWordsSettingItems', label: '热词配置项', type: 'json', rows: 10, placeholder: '请输入热词配置 JSON 数组' }
    ]"
  />
</template>
