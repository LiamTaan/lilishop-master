<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="商品设置"
    description="承接商品审核、商品展示和商品基础业务规则配置。"
    api-path="/manager/setting/setting"
    setting-key="GOODS_SETTING"
    :fields="[
      { key: 'goodsCheck', label: '商品审核开关', type: 'switch' },
      { key: 'smallPictureWidth', label: '小图宽度', type: 'number' },
      { key: 'smallPictureHeight', label: '小图高度', type: 'number' },
      { key: 'abbreviationPictureWidth', label: '缩略图宽度', type: 'number' },
      { key: 'abbreviationPictureHeight', label: '缩略图高度', type: 'number' },
      { key: 'originalPictureWidth', label: '原图宽度', type: 'number' },
      { key: 'originalPictureHeight', label: '原图高度', type: 'number' }
    ]"
  />
</template>
