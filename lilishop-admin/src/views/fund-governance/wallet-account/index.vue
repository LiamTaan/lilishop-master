<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import { getWalletAccountPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "WalletAccount"
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
  { label: "账户数", value: filteredData.value.length, accent: "orange" as const, hint: "当前分页结果" },
  {
    label: "账户余额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.balance || 0), 0),
    accent: "green" as const,
    hint: "钱包总余额"
  },
  {
    label: "可提现总额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.withdrawableAmount || 0), 0),
    accent: "blue" as const,
    hint: "当前可提现金额"
  },
  { label: "钱包治理", value: "已接入", accent: "purple" as const, hint: "钱包账户页" }
]);

const detailItems = computed(() => [
  { label: "会员名称", value: detail.value.memberName || "-" },
  { label: "手机号", value: detail.value.mobile || "-" },
  { label: "账户余额", value: `¥ ${detail.value.balance || "-"}` },
  { label: "可提现金额", value: `¥ ${detail.value.withdrawableAmount || "-"}` },
  { label: "冻结金额", value: `¥ ${detail.value.frozenAmount || "-"}` }
]);

function normalizeWalletRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.memberId,
    memberName: item.memberName || "-",
    mobile: item.mobile || "-",
    balance: item.balance ?? "-",
    withdrawableAmount: item.withdrawableAmount ?? "-",
    frozenAmount: item.frozenAmount ?? "-"
  };
}

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  if (query.keyword) {
    params.memberName = query.keyword;
    params.mobile = query.keyword;
  }
  const res = await getWalletAccountPage(params);
  data.value = extractApiRecords(res).map(normalizeWalletRecord);
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

function exportWalletAccounts() {
  if (!filteredData.value.length) {
    message("暂无可导出的钱包账户数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    会员名称: item.memberName,
    手机号: item.mobile,
    账户余额: item.balance,
    可提现金额: item.withdrawableAmount,
    冻结金额: item.frozenAmount
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "钱包账户台账");
  writeFile(workbook, "钱包账户台账.xlsx");
  message("钱包账户导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="钱包账户台账"
    description="承接平台会员钱包查询、余额查看与资金账户治理。"
    api-path="/manager/wallet/wallet/page"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '余额总览', value: '已强化', type: 'warning' },
      { label: '钱包查询', value: '已接入', type: 'success' },
      { label: '提现金额', value: '已展示', type: 'primary' }
    ]"
    keyword-label="会员/手机号"
    keyword-placeholder="请输入会员名称或手机号"
    :status-options="[]"
    :show-status-filter="false"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportWalletAccounts">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="钱包账户详情" size="38%">
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
