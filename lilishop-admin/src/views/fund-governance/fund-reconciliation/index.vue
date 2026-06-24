<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getFundReconciliationPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getWalletServiceTypeLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "FundReconciliation"
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
  { label: "资金流水数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "变动金额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.money || 0), 0),
    accent: "green" as const,
    hint: "资金金额汇总"
  },
  {
    label: "代理商数",
    value: [...new Set(filteredData.value.map(item => item.agentMemberId))].length,
    accent: "blue" as const,
    hint: "参与资金流水代理商"
  },
  { label: "资金对账", value: "已接入", accent: "purple" as const, hint: "资金核对页" }
]);

const detailItems = computed(() => [
  { label: "流水号", value: detail.value.id || "-" },
  { label: "代理商", value: detail.value.agentName || "-" },
  { label: "业务类型", value: getWalletServiceTypeLabel(detail.value.serviceType) },
  { label: "变动金额", value: `¥ ${detail.value.money || "-"}` },
  { label: "业务描述", value: detail.value.detail || "-" },
  { label: "创建时间", value: detail.value.createTime || "-" }
]);

function normalizeFundRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.sn || "-",
    agentMemberId: item.agentMemberId || item.memberId || "-",
    agentName: item.agentName || item.memberName || "-",
    serviceType: item.serviceType || item.status || "-",
    money: item.money || item.balanceAmount || item.totalAmount || "-",
    detail: item.detail || item.remark || "-",
    createTime: item.createTime || "-"
  };
}

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.keyword = query.keyword;
  if (query.status) params.serviceType = query.status;
  const res = await getFundReconciliationPage(params);
  data.value = extractApiRecords(res).map(normalizeFundRecord);
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
    title="资金对账"
    description="承接平台代理商资金流水查询、业务分类与资金核对。"
    api-path="/manager/reconciliation/fund"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '余额充值', value: 'WALLET_RECHARGE' },
      { label: '余额支付', value: 'WALLET_PAY' },
      { label: '余额退款', value: 'WALLET_REFUND' },
      { label: '余额提现', value: 'WALLET_WITHDRAWAL' },
      { label: '佣金提成', value: 'WALLET_COMMISSION' }
    ]"
    :quick-actions="[
      { label: '资金流水', value: '已接入', type: 'warning' },
      { label: '金额统计', value: '已强化', type: 'success' },
      { label: '业务分类', value: '真实字段', type: 'primary' }
    ]"
    keyword-label="代理商/描述"
    keyword-placeholder="请输入代理商、会员ID或业务描述"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="资金流水详情" size="40%">
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
