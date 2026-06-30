<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import { getVerificationRecordPage } from "@/api/order-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  formatAdminDateTime,
  getVerifyStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "VerificationRecordManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  storeKeyword: ""
});

function normalizeVerificationRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || `${item.orderSn || ""}-${item.verifyTime || ""}`,
    orderSn: item.orderSn || item.sn || "-",
    verificationCode: item.verificationCode || item.code || "-",
    verifyStatus: item.resultType || item.verifyStatus || item.status || "-",
    storeName: item.storeName || "-",
    operatorName: item.operatorName || item.createBy || "-",
    verifyTime: formatAdminDateTime(item.verifyTime || item.createTime || "-")
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.storeKeyword ? String(item.storeName).includes(extraFilters.storeKeyword) : true
  )
);

const summaryCards = computed(() => [
  { label: "核销记录数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "成功核销",
    value: filteredData.value.filter(item => String(item.verifyStatus).toUpperCase().includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "已完成核销"
  },
  {
    label: "核销门店数",
    value: [...new Set(filteredData.value.map(item => item.storeName))].length,
    accent: "blue" as const,
    hint: "参与核销门店"
  },
  { label: "核销治理", value: "已接入", accent: "purple" as const, hint: "核销记录页" }
]);

const detailItems = computed(() => [
  { label: "订单号", value: detail.value.orderSn || "-" },
  { label: "核销码", value: detail.value.verificationCode || "-" },
  { label: "核销状态", value: getVerifyStatusLabel(detail.value.verifyStatus) },
  { label: "核销门店", value: detail.value.storeName || "-" },
  { label: "核销人", value: detail.value.operatorName || "-" },
  { label: "核销时间", value: detail.value.verifyTime || "-" }
]);

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  if (query.keyword) params.orderSn = query.keyword;
  if (query.status) params.resultType = query.status;
  const res = await getVerificationRecordPage(params);
  data.value = extractApiRecords(res).map(normalizeVerificationRecord);
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

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
}

function exportVerificationRecords() {
  if (!filteredData.value.length) {
    return;
  }
  const table = filteredData.value.map(item => ({
    订单号: item.orderSn,
    核销码: item.verificationCode,
    核销状态: getVerifyStatusLabel(item.verifyStatus),
    核销门店: item.storeName,
    核销人: item.operatorName,
    核销时间: item.verifyTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "核销记录台账");
  writeFile(workbook, "核销记录台账.xlsx");
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="核销记录台账"
    description="承接平台核销查询、核销状态统计与异常核销治理。"
    api-path="/manager/other/verificationRecord"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '核销状态', value: '已接入', type: 'warning' },
      { label: '门店筛选', value: '前端补充', type: 'success' },
      { label: '记录台账', value: '已强化', type: 'primary' }
    ]"
    :status-options="[
      { label: '核销成功', value: 'SUCCESS' },
      { label: '核销失败', value: 'FAIL' }
    ]"
    keyword-label="订单号/核销码"
    keyword-placeholder="请输入订单号或核销码"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button :disabled="!filteredData.length" @click="exportVerificationRecords">
        导出
      </el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="核销门店">
        <el-input
          v-model="extraFilters.storeKeyword"
          placeholder="请输入核销门店"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="核销详情" size="38%">
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
