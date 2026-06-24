<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="积分设置"
    description="承接积分基础参数、积分说明和积分规则配置。"
    api-path="/manager/setting/setting"
    setting-key="POINT_SETTING"
    :fields="[
      { key: 'register', label: '注册赠送积分', type: 'number' },
      { key: 'consumer', label: '消费 1 元赠送积分', type: 'number' },
      { key: 'money', label: '多少积分抵 1 元', type: 'number' },
      { key: 'signIn', label: '每日签到积分', type: 'number' },
      { key: 'comment', label: '评价积分', type: 'number' },
      { key: 'pointSettingItems', label: '积分规则项', type: 'json', rows: 8, placeholder: '请输入积分规则项 JSON 数组' }
    ]"
  />
</template>
