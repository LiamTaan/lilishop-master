<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="物流设置"
    description="承接物流查询、电子面单及物流展示基础配置。"
    api-path="/manager/setting/setting"
    setting-key="LOGISTICS_SETTING"
    :fields="[
      { key: 'type', label: '物流查询类型' },
      { key: 'kdniaoEbusinessID', label: '快递鸟商户号' },
      { key: 'kdniaoAppKey', label: '快递鸟 AppKey', type: 'password' },
      { key: 'requestType', label: '调用端口' },
      { key: 'kuaidi100Customer', label: '快递100 授权码', type: 'password' },
      { key: 'kuaidi100Key', label: '快递100 Key', type: 'password' },
      { key: 'clientCode', label: '顺丰顾客编码' },
      { key: 'checkWord', label: '顺丰校验码', type: 'password' },
      { key: 'callUrl', label: '顺丰请求地址' },
      { key: 'templateCode', label: '顺丰打印模板' },
      { key: 'monthlyCardNo', label: '顺丰月结号' }
    ]"
  />
</template>
