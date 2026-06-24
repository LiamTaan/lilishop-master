<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getWithdrawApplyPage } from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getWithdrawApplyStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "WithdrawApply"
});

const data = ref<Record<string, any>[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  accountKeyword: ""
});

function normalizeWithdrawRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.applySn || item.sn,
    applySn: item.applySn || item.sn || "-",
    memberName: item.memberName || item.username || "-",
    withdrawAmount: item.withdrawAmount || item.amount || "-",
    accountType: item.accountType || item.bankName || item.withdrawWay || "-",
    applyStatus: item.applyStatus || item.status || "-",
    createTime: item.createTime || item.applyTime || "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.accountKeyword ? String(item.accountType).includes(extraFilters.accountKeyword) : true
  )
);

const summaryCards = computed(() => {
  const totalAmount = filteredData.value.reduce(
    (sum, item) => sum + Number(item.withdrawAmount || 0),
    0
  );
  return [
    { label: "申请总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
    {
      label: "待审核申请",
      value: filteredData.value.filter(item => String(item.applyStatus).toUpperCase() === "APPLY").length,
      accent: "blue" as const,
      hint: "待平台处理"
    },
    { label: "提现总额", value: totalAmount, accent: "green" as const, hint: "筛选结果汇总" },
    { label: "审核流程", value: "已接入", accent: "purple" as const, hint: "提现治理页" }
  ];
});

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.applySn = query.keyword;
  if (query.status) params.applyStatus = query.status;
  const res = await getWithdrawApplyPage(params);
  data.value = extractApiRecords(res).map(normalizeWithdrawRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.accountKeyword = "";
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="提现审核"
    description="承接平台提现申请分页、审核与处理备注治理。"
    api-path="/manager/wallet/withdrawApply"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '申请中', value: 'APPLY' },
      { label: '审核通过', value: 'VIA_AUDITING' },
      { label: '审核未通过', value: 'FAIL_AUDITING' },
      { label: '提现成功', value: 'SUCCESS' },
      { label: '提现失败', value: 'ERROR' }
    ]"
    :quick-actions="[
      { label: '申请状态', value: '已接入', type: 'warning' },
      { label: '收款方式', value: '前端补充', type: 'success' },
      { label: '金额展示', value: '已强化', type: 'primary' }
    ]"
    keyword-label="申请单号"
    keyword-placeholder="请输入申请单号"
    status-label="申请状态"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="收款方式">
        <el-input
          v-model="extraFilters.accountKeyword"
          placeholder="请输入收款方式"
          clearable
        />
      </el-form-item>
    </template>
  </WholesaleAdminPage>
</template>
