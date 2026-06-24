<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="订单设置"
    description="承接下单超时、自动收货和订单业务基础规则配置。"
    api-path="/manager/setting/setting"
    setting-key="ORDER_SETTING"
    :fields="[
      { key: 'autoCancel', label: '自动取消分钟', type: 'number' },
      { key: 'autoReceive', label: '自动收货天数', type: 'number' },
      { key: 'closeAfterSale', label: '完成订单允许退单天数', type: 'number' },
      { key: 'autoEvaluation', label: '自动评价天数', type: 'number' },
      { key: 'autoCancelAfterSale', label: '售后自动取消天数', type: 'number' },
      { key: 'autoAfterSaleReview', label: '退单自动审核天数', type: 'number' },
      { key: 'autoAfterSaleComplete', label: '退单自动确认收货天数', type: 'number' },
      { key: 'closeComplaint', label: '完成订单允许投诉天数', type: 'number' }
    ]"
  />
</template>
