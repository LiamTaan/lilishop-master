<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import { getProfitSharingBalance } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "ProfitSharingBalance"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const summaryCards = computed(() => {
  const first = data.value[0] || {};
  return [
    {
      label: "角色维度",
      value: data.value.length,
      accent: "orange" as const,
      hint: "当前概览条目数"
    },
    {
      label: "待结算金额",
      value: first.pendingAmount || 0,
      accent: "blue" as const,
      hint: "概览对象字段"
    },
    {
      label: "可提现金额",
      value: first.withdrawableAmount || 0,
      accent: "green" as const,
      hint: "概览对象字段"
    },
    {
      label: "分账治理",
      value: "已接入",
      accent: "purple" as const,
      hint: "/manager/profitsharing/balance"
    }
  ];
});

const detailItems = computed(() => [
  { label: "角色类型", value: detail.value.roleType || "-" },
  { label: "账户数量", value: detail.value.accountCount || 0 },
  { label: "待结算金额", value: `¥ ${detail.value.pendingAmount || "-"}` },
  { label: "已结算金额", value: `¥ ${detail.value.settledAmount || "-"}` },
  { label: "可提现金额", value: `¥ ${detail.value.withdrawableAmount || "-"}` }
]);

function normalizeBalanceRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.roleType || item.memberId,
    roleType: item.roleType || item.accountType || "-",
    accountCount: item.accountCount || item.totalCount || item.memberCount || 0,
    pendingAmount: item.pendingAmount || item.unsettledAmount || "-",
    settledAmount: item.settledAmount || item.totalSettledAmount || "-",
    withdrawableAmount: item.withdrawableAmount || item.availableAmount || "-"
  };
}

async function loadData() {
  const res = await getProfitSharingBalance();
  data.value = extractApiRecords(res).map(normalizeBalanceRecord);
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

function exportProfitSharingBalance() {
  if (!data.value.length) {
    message("暂无可导出的分账概览数据", { type: "warning" });
    return;
  }
  const table = data.value.map(item => ({
    角色类型: item.roleType,
    账户数量: item.accountCount,
    待结算金额: item.pendingAmount,
    已结算金额: item.settledAmount,
    可提现金额: item.withdrawableAmount
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "分账治理概览");
  writeFile(workbook, "分账治理概览.xlsx");
  message("分账概览导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="分账治理概览"
    description="承接平台分账余额、待结算金额与角色维度聚合概览。当前后端返回概览对象，页面按单行概览展示。"
    api-path="/manager/profitsharing/balance"
    :columns="columns"
    :data="data"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    keyword-label="角色/账户"
    keyword-placeholder="请输入角色或账户关键字"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportProfitSharingBalance">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="分账概览详情" size="38%">
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
