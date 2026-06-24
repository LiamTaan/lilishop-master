<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    description: string;
    summaryCards?: Array<{
      label: string;
      value: string | number;
      accent?: "orange" | "blue" | "green" | "purple";
      hint?: string;
    }>;
    tips?: string[];
    apiPath?: string;
  }>(),
  {
    summaryCards: () => [],
    tips: () => [],
    apiPath: ""
  }
);
</script>

<template>
  <div class="admin-module-shell">
    <section class="admin-module-shell__content">
      <slot />
    </section>
  </div>
</template>

<style scoped>
.admin-module-shell {
  display: flex;
  flex-direction: column;
}

.admin-module-shell__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

:deep(.search-form) {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 18px;
  align-items: flex-end;
  padding: 18px 20px !important;
  background: #fff !important;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
  overflow: visible !important;
}

:deep(.search-form .el-form-item) {
  margin: 0 !important;
}

:deep(.search-form .el-form-item__label) {
  color: #5b6270;
  font-weight: 500;
}

:deep(.search-form .el-form-item:last-child) {
  margin-left: auto !important;
}

:deep(.search-form .el-form-item:last-child .el-form-item__content) {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

:deep(.search-form .el-input__wrapper),
:deep(.search-form .el-select__wrapper) {
  min-width: 210px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #ebeef4 inset;
}

:deep(.search-form + .w-full) {
  padding: 0 !important;
  overflow: hidden;
  background: #fff !important;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

:deep(.search-form + .w-full > .flex.justify-between.w-full.h-15.p-4) {
  height: auto;
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0f2f5;
}

:deep(.search-form + .w-full .pure-table) {
  --el-table-header-bg-color: #f5f6f8;
  --el-table-row-hover-bg-color: #fff8f1;
}

:deep(.search-form + .w-full .el-table) {
  border-radius: 0;
}

:deep(.search-form + .w-full .el-table th.el-table__cell) {
  color: #4f5560;
  font-weight: 700;
}

:deep(.search-form + .w-full .el-pagination) {
  justify-content: flex-end !important;
  margin: 20px 0 10px;
}

:deep(.admin-module-shell .el-button--primary) {
  --el-button-bg-color: #ff7a1a;
  --el-button-border-color: #ff7a1a;
  --el-button-hover-bg-color: #ff8d39;
  --el-button-hover-border-color: #ff8d39;
}

@media (width <= 900px) {
  :deep(.search-form .el-form-item:last-child) {
    width: 100%;
    margin-left: 0 !important;
  }

  :deep(.search-form .el-form-item:last-child .el-form-item__content) {
    justify-content: flex-start;
  }
}
</style>
