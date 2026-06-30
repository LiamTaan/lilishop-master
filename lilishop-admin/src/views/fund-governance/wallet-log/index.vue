<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import { getMemberWallet, getWalletLogPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getWalletServiceTypeLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "WalletLog"
});

const data = ref<Record<string, any>[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const filteredData = computed(() =>
  data.value.filter(item =>
    query.status ? String(item.serviceType).toUpperCase() === query.status.toUpperCase() : true
  )
);
const summaryCards = computed(() => [
  { label: "流水记录数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "流水金额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.amount || 0), 0),
    accent: "green" as const,
    hint: "变动金额汇总"
  },
  {
    label: "会员覆盖",
    value: [...new Set(filteredData.value.map(item => item.memberName))].length,
    accent: "blue" as const,
    hint: "参与流水会员数"
  },
  { label: "资金流水", value: "已接入", accent: "purple" as const, hint: "流水治理页" }
]);

function normalizeWalletLogRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.sn,
    sn: item.sn || item.logSn || "-",
    memberId: item.memberId || item.userId || item.uid || "",
    memberName: item.memberName || item.username || "-",
    serviceType: item.serviceType || item.logType || item.businessType || "-",
    serviceTypeLabel: getWalletServiceTypeLabel(
      item.serviceType || item.logType || item.businessType || "-"
    ),
    amount: item.money ?? item.amount ?? item.price ?? "-",
    balanceAfter: item.balanceAfter || item.balance || "-",
    createTime: item.createTime || "-"
  };
}

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  if (query.keyword) params.memberName = query.keyword;
  const res = await getWalletLogPage(params);
  const rows = extractApiRecords(res).map(normalizeWalletLogRecord);
  const memberIds = [...new Set(rows.map(item => item.memberId).filter(Boolean))];
  const walletEntries = await Promise.all(
    memberIds.map(async memberId => {
      try {
        const walletRes = await getMemberWallet(String(memberId));
        const wallet = extractApiPayload(walletRes) as Record<string, any>;
        return [memberId, wallet?.memberWallet ?? "-"] as const;
      } catch (_error) {
        return [memberId, "-"] as const;
      }
    })
  );
  const walletMap = new Map(walletEntries);
  data.value = rows.map(item => ({
    ...item,
    balanceAfter: walletMap.get(item.memberId) ?? item.balanceAfter ?? "-"
  }));
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

function exportWalletLogs() {
  if (!filteredData.value.length) {
    message("暂无可导出的余额记录", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    流水号: item.sn,
    会员名称: item.memberName,
    流水类型: item.serviceTypeLabel,
    变动金额: item.amount,
    当前余额: item.balanceAfter,
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "余额记录");
  writeFile(workbook, "余额记录.xlsx");
  message("余额记录导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="余额记录"
    description="承接钱包流水、充值记录、分账入账与提现流水查看。"
    api-path="/manager/wallet/log"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '流水类型', value: '已接入', type: 'warning' },
      { label: '变动金额', value: '已强化', type: 'success' },
      { label: '流水治理', value: '已展示', type: 'primary' }
    ]"
    :status-options="[
      { label: '余额充值', value: 'WALLET_RECHARGE' },
      { label: '余额支付', value: 'WALLET_PAY' },
      { label: '余额退款', value: 'WALLET_REFUND' },
      { label: '余额提现', value: 'WALLET_WITHDRAWAL' },
      { label: '佣金提成', value: 'WALLET_COMMISSION' }
    ]"
    keyword-label="会员名称"
    keyword-placeholder="请输入会员名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportWalletLogs">导出</el-button>
    </template>
  </WholesaleAdminPage>
</template>
