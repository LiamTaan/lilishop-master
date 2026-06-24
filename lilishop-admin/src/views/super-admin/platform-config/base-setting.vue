<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="基础设置"
    description="承接平台基础信息、站点信息和默认展示配置。"
    api-path="/manager/setting/setting"
    setting-key="BASE_SETTING"
    :fields="[
      { key: 'siteName', label: '站点名称' },
      { key: 'icp', label: 'ICP备案号' },
      { key: 'domainLogo', label: '平台管理 Logo' },
      { key: 'domainIcon', label: '平台管理 Icon' },
      { key: 'buyerSideLogo', label: '买家端 Logo' },
      { key: 'buyerSideIcon', label: '买家端 Icon' },
      { key: 'storeSideLogo', label: '商家端 Logo' },
      { key: 'storeSideIcon', label: '商家端 Icon' },
      { key: 'staticPageAddress', label: 'PC 站点地址' },
      { key: 'staticPageWapAddress', label: 'WAP 站点地址' }
    ]"
  />
</template>
