<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
  apiPath: string;
}>();
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="page-intro__header">
        <div>
          <h2>{{ title }}</h2>
          <p>{{ description }}</p>
        </div>
        <el-tag type="info">{{ apiPath }}</el-tag>
      </div>
    </template>
    <el-empty description="页面壳子已创建，下一步接查询表单、列表和审核动作" />
  </el-card>
</template>

<style scoped>
.page-intro__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-intro__header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.page-intro__header p {
  margin: 0;
  color: var(--el-text-color-secondary);
}
</style>
