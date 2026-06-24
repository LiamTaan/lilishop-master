<script setup lang="ts">
import { computed } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { createDefaultColumns, useSuperAdminPage } from "./menu-runtime";

const props = defineProps<{
  title: string;
  description: string;
  apiPath: string;
  keywordLabel: string;
  keywordPlaceholder: string;
  pageConfig: Parameters<typeof useSuperAdminPage>[0];
  quickActions?: Array<{
    label: string;
    value?: string;
    type?: "primary" | "success" | "warning" | "info" | "danger";
  }>;
}>();

const pageState = useSuperAdminPage(props.pageConfig);

const columns = computed<TableColumnList>(() => createDefaultColumns(props.title));
const tableData = computed(() => pageState.rows.value);
const summaryCards = computed(() => pageState.summaryCards.value);
const detailVisible = computed({
  get: () => pageState.detailVisible.value,
  set: value => {
    pageState.detailVisible.value = value;
  }
});
const detailEntries = computed(() => pageState.detailEntries.value);
</script>

<template>
  <WholesaleAdminPage
    :title="props.title"
    :description="props.description"
    :api-path="props.apiPath"
    :columns="columns"
    :data="tableData"
    :summary-cards="summaryCards"
    :status-options="pageState.statusOptions"
    :quick-actions="
      props.quickActions || [
        { label: '接口承接', value: '已接入', type: 'primary' },
        { label: '详情抽屉', value: '已统一', type: 'success' },
        { label: '动作反馈', value: '已接管', type: 'warning' }
      ]
    "
    :keyword-label="props.keywordLabel"
    :keyword-placeholder="props.keywordPlaceholder"
    @search="pageState.handleSearch"
    @reset="pageState.handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="pageState.showDetail(row)">
        详情
      </el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer
    v-model="detailVisible"
    :title="`${props.title}详情`"
    size="520px"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item
        v-for="entry in detailEntries"
        :key="entry.label"
        :label="entry.label"
      >
        {{ entry.value }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
