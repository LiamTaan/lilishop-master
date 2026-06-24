<script setup lang="ts">
import SettingCenter from "./setting-center.vue";
</script>

<template>
  <SettingCenter
    title="对象存储配置"
    description="承接 OSS 存储配置、Bucket 及访问域名治理。"
    api-path="/manager/setting/setting"
    setting-key="OSS_SETTING"
    :fields="[
      { key: 'type', label: 'OSS 类型' },
      { key: 'aliyunOSSEndPoint', label: '阿里云 Endpoint' },
      { key: 'aliyunOSSBucketName', label: '阿里云 Bucket' },
      { key: 'aliyunOSSAccessKeyId', label: '阿里云 AccessKeyId', type: 'password' },
      { key: 'aliyunOSSAccessKeySecret', label: '阿里云 AccessKeySecret', type: 'password' },
      { key: 'm_endpoint', label: 'MinIO 服务地址' },
      { key: 'm_frontUrl', label: 'MinIO 前端访问地址' },
      { key: 'm_accessKey', label: 'MinIO AccessKey', type: 'password' },
      { key: 'm_secretKey', label: 'MinIO SecretKey', type: 'password' },
      { key: 'm_bucketName', label: 'MinIO Bucket' },
      { key: 'huaweicloudOBSAccessKey', label: '华为云 AccessKey', type: 'password' },
      { key: 'huaweicloudOBSSecretKey', label: '华为云 SecretKey', type: 'password' },
      { key: 'huaweicloudOBSEndPoint', label: '华为云 Endpoint' },
      { key: 'huaweicloudOBSBucketName', label: '华为云 Bucket' },
      { key: 'tencentCOSSecretId', label: '腾讯云 SecretId', type: 'password' },
      { key: 'tencentCOSSecretKey', label: '腾讯云 SecretKey', type: 'password' },
      { key: 'tencentCOSRegion', label: '腾讯云 Region' },
      { key: 'tencentCOSBucket', label: '腾讯云 Bucket' },
      { key: 'tencentCOSEndPoint', label: '腾讯云 Endpoint' }
    ]"
  />
</template>
