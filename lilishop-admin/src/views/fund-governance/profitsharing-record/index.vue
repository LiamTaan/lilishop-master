<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getProfitSharingRecordPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "ProfitSharingRecord"
});

const data = ref<Record<string, any>[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  storeKeyword: ""
});

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.storeKeyword
      ? String(item.storeName).includes(extraFilters.storeKeyword)
      : true
  )
);

const summaryCards = computed(() => [
  {
    label: "分账明细",
    value: filteredData.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "已结算",
    value: filteredData.value.filter(item =>
      ["FINISHED", "SUCCESS", "DONE", "COMPLETED"].includes(
        String(item.settlementStatus).toUpperCase()
      )
    ).length,
    accent: "green" as const,
    hint: "已完成结算"
  },
  {
    label: "分账金额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.amount || 0), 0),
    accent: "blue" as const,
    hint: "当前分账金额汇总"
  },
  {
    label: "分账治理",
    value: "已接入",
    accent: "purple" as const,
    hint: "/manager/profitsharing/record"
  }
]);

function normalizeRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.recordId || item.orderSn || item.sn,
    orderSn: item.orderSn || item.sn || "-",
    storeName: item.storeName || "-",
    billPrice: item.billPrice ?? item.amount ?? 0,
    refundPrice: item.refundPrice ?? 0,
    amount:
      item.commissionAmount ??
      item.amount ??
      item.profitAmount ??
      item.shareAmount ??
      "-",
    auditStatus: item.billStatus || item.auditStatus || item.status || "-",
    settlementStatus: item.settlementStatus || item.billStatus || "-",
    settleTime: item.settleTime || item.settlementTime || item.createTime || "-"
  };
}

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.orderSn = query.keyword;
  if (query.status) params.settlementStatus = query.status;
  const res = await getProfitSharingRecordPage(params);
  data.value = extractApiRecords(res).map(normalizeRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.storeKeyword = "";
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="分账明细"
    description="承接平台分账记录查询、账单状态与结算状态治理。"
    api-path="/manager/profitsharing/record"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '处理中', value: 'PROCESSING' },
      { label: '分账完成', value: 'FINISHED' },
      { label: '待订单完成', value: 'WAIT_COMPLETE' },
      { label: '订单取消', value: 'ORDER_CANCEL' }
    ]"
    :quick-actions="[
      { label: '账单状态', value: '已展示', type: 'warning' },
      { label: '店铺筛选', value: '前端补充', type: 'success' },
      { label: '金额统计', value: '已强化', type: 'primary' }
    ]"
    keyword-label="账单号"
    keyword-placeholder="请输入账单号"
    status-label="结算状态"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="店铺名称">
        <el-input
          v-model="extraFilters.storeKeyword"
          placeholder="请输入店铺名称"
          clearable
        />
      </el-form-item>
    </template>
  </WholesaleAdminPage>
</template>
