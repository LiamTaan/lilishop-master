<script setup lang="ts">
import { computed, reactive, useSlots, watch } from "vue";

type StatusOption = { label: string; value: string };
type QuickAction = {
  label: string;
  value?: string;
  type?: "primary" | "success" | "warning" | "info" | "danger";
};

const props = withDefaults(
  defineProps<{
    title: string;
    description: string;
    apiPath: string;
    columns: TableColumnList;
    data: Record<string, any>[];
    keywordLabel?: string;
    keywordPlaceholder?: string;
    statusLabel?: string;
    statusOptions?: StatusOption[];
    quickActions?: QuickAction[];
    primaryActionText?: string;
    secondaryActionText?: string;
    showStatusFilter?: boolean;
    selectable?: boolean;
    pageSize?: number;
    pageSizes?: number[];
  }>(),
  {
    keywordLabel: "关键字",
    keywordPlaceholder: "请输入关键字",
    statusLabel: "状态",
    primaryActionText: "查询",
    secondaryActionText: "重置",
    showStatusFilter: true,
    selectable: false,
    pageSize: 20,
    pageSizes: () => [20, 50, 100],
    statusOptions: () => [
      { label: "待处理", value: "PENDING" },
      { label: "进行中", value: "PROCESSING" },
      { label: "已完成", value: "DONE" },
      { label: "已关闭", value: "CLOSED" }
    ],
    quickActions: () => []
  }
);

const emit = defineEmits<{
  search: [payload: { keyword: string; status: string }];
  reset: [];
  selectionChange: [rows: Record<string, any>[]];
}>();

const slots = useSlots();
const queryForm = reactive({
  keyword: "",
  status: ""
});
const pagination = reactive({
  pageSize: props.pageSize,
  currentPage: 1
});

const tableColumns = computed(() => {
  if (!props.selectable) return props.columns;
  return [
    {
      type: "selection",
      width: 54,
      reserveSelection: true,
      align: "center"
    },
    ...props.columns
  ] as TableColumnList;
});

const pagedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize;
  return props.data.slice(start, start + pagination.pageSize);
});

const tablePagination = computed(() => ({
  pageSize: pagination.pageSize,
  currentPage: pagination.currentPage,
  pageSizes: props.pageSizes,
  total: props.data.length,
  align: "right" as const,
  background: true,
  size: "default" as const
}));

watch(
  () => [props.data.length, pagination.pageSize],
  () => {
    const maxPage = Math.max(1, Math.ceil(props.data.length / pagination.pageSize));
    if (pagination.currentPage > maxPage) {
      pagination.currentPage = 1;
    }
  }
);

function handleSearch() {
  pagination.currentPage = 1;
  emit("search", {
    keyword: queryForm.keyword.trim(),
    status: queryForm.status
  });
}

function handleReset() {
  queryForm.keyword = "";
  queryForm.status = "";
  pagination.currentPage = 1;
  emit("reset");
}

function handlePageSizeChange(size: number) {
  pagination.pageSize = size;
  pagination.currentPage = 1;
}

function handlePageCurrentChange(page: number) {
  pagination.currentPage = page;
}
</script>

<template>
  <div class="wholesale-page">
    <section class="page-card page-card--filters">
      <el-form :inline="true" :model="queryForm" class="page-filter-form">
        <el-form-item :label="props.keywordLabel">
          <el-input
            v-model="queryForm.keyword"
            :placeholder="props.keywordPlaceholder"
            clearable
          />
        </el-form-item>
        <el-form-item v-if="props.showStatusFilter" :label="props.statusLabel">
          <el-select
            v-model="queryForm.status"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="option in props.statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <slot name="filters-extra" />
        <el-form-item class="page-filter-form__actions">
          <el-button type="primary" @click="handleSearch">
            {{ props.primaryActionText }}
          </el-button>
          <el-button @click="handleReset">{{ props.secondaryActionText }}</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="page-card page-card--table">
      <div class="page-table-header">
        <div class="page-table-header__left">
          <h3 class="page-table-header__title">{{ props.title }}</h3>
        </div>
        <div v-if="slots['table-extra']" class="page-table-header__right">
          <slot name="table-extra" />
        </div>
      </div>

      <pure-table
        row-key="id"
        showOverflowTooltip
        table-layout="auto"
        :data="pagedData"
        :columns="tableColumns"
        :pagination="tablePagination"
        :header-cell-style="{
          background: '#f5f6f8',
          color: '#4f5560',
          fontWeight: 700
        }"
        @page-size-change="handlePageSizeChange"
        @page-current-change="handlePageCurrentChange"
        @selection-change="rows => emit('selectionChange', rows)"
      >
        <template v-if="slots.operation" #operation="scope">
          <slot name="operation" v-bind="scope" />
        </template>
      </pure-table>
    </section>
  </div>
</template>

<style scoped>
.wholesale-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-card {
  padding: 18px 20px;
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.page-card--filters {
  padding-bottom: 18px;
}

.page-filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 18px;
  align-items: end;
}

:deep(.page-filter-form .el-form-item) {
  flex: 1 1 240px;
  min-width: 220px;
  margin: 0 !important;
}

:deep(.page-filter-form .el-form-item__label) {
  color: #5b6270;
  padding-right: 12px;
  font-weight: 500;
  white-space: nowrap;
}

:deep(.page-filter-form .el-form-item__content) {
  display: flex;
  flex: 1;
  min-width: 0;
}

:deep(.page-filter-form .el-input),
:deep(.page-filter-form .el-select),
:deep(.page-filter-form .el-date-editor) {
  width: 100%;
}

:deep(.page-filter-form .el-input__wrapper),
:deep(.page-filter-form .el-select__wrapper) {
  width: 100%;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #ebeef4 inset;
}

.page-filter-form__actions {
  flex: 0 0 auto !important;
  min-width: auto !important;
  margin-left: auto !important;
}

:deep(.page-filter-form__actions .el-form-item__content) {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.page-table-header {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-table-header__left,
.page-table-header__right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.page-table-header__left {
  flex: 1;
  min-width: 0;
}

.page-table-header__right {
  justify-content: flex-end;
  margin-left: auto;
}

.page-table-header__title {
  margin: 0;
  color: #2f3135;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
}

:deep(.page-card .pure-table) {
  --el-table-header-bg-color: #f5f6f8;
  --el-table-row-hover-bg-color: #fff8f1;
}

:deep(.page-card .el-table) {
  border-radius: 12px;
}

:deep(.page-card .el-table__empty-block) {
  min-height: 88px;
}

:deep(.page-card .el-table td.el-table__cell) {
  color: #4b515c;
}

:deep(.page-card .el-button--primary) {
  --el-button-bg-color: #ff7a1a;
  --el-button-border-color: #ff7a1a;
  --el-button-hover-bg-color: #ff8d39;
  --el-button-hover-border-color: #ff8d39;
}

:deep(.page-card .el-pagination) {
  justify-content: flex-end;
  margin-top: 24px;
}

@media (width <= 900px) {
  .page-filter-form__actions {
    width: 100%;
    margin-left: 0 !important;
  }

  :deep(.page-filter-form__actions .el-form-item__content) {
    justify-content: flex-start;
  }

  .page-table-header {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (width <= 640px) {
  :deep(.page-filter-form .el-form-item) {
    flex-basis: 100%;
    min-width: 0;
  }
}
</style>
