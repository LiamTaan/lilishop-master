<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getStoreApplyPage, getStoreAuditLog } from "@/api/store-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "StoreAuditLog"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const filteredData = computed(() => data.value);
const summaryCards = computed(() => [
  { label: "审核记录数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "审核店铺数",
    value: [...new Set(filteredData.value.map(item => item.storeName))].length,
    accent: "blue" as const,
    hint: "涉及店铺数量"
  },
  {
    label: "操作人覆盖",
    value: [...new Set(filteredData.value.map(item => item.operatorName))].length,
    accent: "green" as const,
    hint: "参与审核人员"
  },
  { label: "审核轨迹", value: "已接入", accent: "purple" as const, hint: "历史流转日志" }
]);

const detailItems = computed(() => [
  { label: "店铺名称", value: detail.value.storeName || "-" },
  { label: "审核动作", value: detail.value.auditAction || "-" },
  { label: "审核结果", value: detail.value.auditResult || "-" },
  { label: "审核备注", value: detail.value.auditRemark || "-" },
  { label: "操作人", value: detail.value.operatorName || "-" },
  { label: "操作时间", value: detail.value.operateTime || "-" }
]);

async function loadData() {
  const storeRes = await getStoreApplyPage({
    pageNumber: 1,
    pageSize: 20,
    ...(query.keyword ? { storeName: query.keyword } : {}),
    ...(query.status
      ? { storeDisable: query.status, storeStatus: query.status }
      : {})
  });
  const stores = extractApiRecords(storeRes);
  if (!stores.length) {
    data.value = [];
    return;
  }

  const rows = await Promise.all(
    stores.map(async (store: Record<string, any>) => {
      const storeId = store.storeId || store.id;
      if (!storeId) return [];
      const logRes = await getStoreAuditLog(storeId);
      const logs = extractApiPayload<any[]>(logRes) ?? [];
      return logs.map((item: Record<string, any>, index: number) => ({
        id: item.id || `${storeId}-${index}`,
        storeName: store.storeName || "-",
        auditAction: item.auditType || item.auditAction || "-",
        auditResult: item.auditStatus || item.auditResult || "-",
        auditRemark: item.remark || item.auditRemark || "-",
        operatorName: item.operatorName || item.createBy || "-",
        operateTime: item.createTime || item.operateTime || "-"
      }));
    })
  );

  data.value = rows.flat();
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  loadData();
}

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="账号审核记录"
    description="承接店铺审核轨迹、备注与历史流转记录查看。"
    api-path="/manager/store/store/audit/log/{storeId}"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '审核轨迹', value: '已接入', type: 'warning' },
      { label: '操作人覆盖', value: '已统计', type: 'success' },
      { label: '历史流转', value: '已展示', type: 'primary' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="审核日志详情" size="38%">
    <el-descriptions :column="1" border>
      <el-descriptions-item
        v-for="item in detailItems"
        :key="item.label"
        :label="item.label"
      >
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
