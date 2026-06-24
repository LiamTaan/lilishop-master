<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getProcurementReconciliationPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getProcurementStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "ProcurementReconciliation"
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
  { label: "采购单数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "采购金额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0),
    accent: "green" as const,
    hint: "采购金额汇总"
  },
  {
    label: "采购数量",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.totalQuantity || 0), 0),
    accent: "blue" as const,
    hint: "采购商品数量汇总"
  },
  { label: "采购对账", value: "已接入", accent: "purple" as const, hint: "采购账单页" }
]);

const detailItems = computed(() => [
  { label: "采购单号", value: detail.value.orderSn || "-" },
  { label: "代理商", value: detail.value.agentName || "-" },
  { label: "店铺名称", value: detail.value.storeName || "-" },
  { label: "采购金额", value: `¥ ${detail.value.totalAmount || "-"}` },
  { label: "采购数量", value: detail.value.totalQuantity || 0 },
  { label: "采购状态", value: getProcurementStatusLabel(detail.value.status) },
  { label: "创建时间", value: detail.value.createTime || "-" },
  { label: "备注", value: detail.value.remark || "-" }
]);

function normalizeProcurementRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.orderId || item.orderSn,
    orderId: item.orderId || item.id || "-",
    orderSn: item.orderSn || item.reconciliationSn || item.sn || "-",
    agentName: item.agentName || item.memberName || "-",
    storeName: item.storeName || "-",
    totalAmount: item.totalAmount || item.purchaseAmount || "-",
    totalQuantity: item.totalQuantity || item.purchaseCount || item.totalCount || 0,
    status: item.status || "-",
    createTime: item.createTime || "-",
    remark: item.remark || "-"
  };
}

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.keyword = query.keyword;
  if (query.status) params.status = query.status;
  const res = await getProcurementReconciliationPage(params);
  data.value = extractApiRecords(res).map(normalizeProcurementRecord);
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
    title="采购对账"
    description="承接平台代理商采购明细查询、状态跟踪与采购核对。"
    api-path="/manager/reconciliation/purchase"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待提交', value: 'DRAFT' },
      { label: '已提交', value: 'SUBMITTED' },
      { label: '待入库', value: 'PENDING_INBOUND' },
      { label: '部分入库', value: 'PARTIAL_INBOUND' },
      { label: '已完成', value: 'COMPLETED' },
      { label: '已关闭', value: 'CLOSED' },
      { label: '已拒绝', value: 'REJECTED' }
    ]"
    :quick-actions="[
      { label: '采购单查询', value: '已接入', type: 'warning' },
      { label: '金额统计', value: '已强化', type: 'success' },
      { label: '状态核对', value: '真实状态', type: 'primary' }
    ]"
    keyword-label="代理商/采购单"
    keyword-placeholder="请输入代理商、店铺或采购单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="采购明细详情" size="40%">
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
