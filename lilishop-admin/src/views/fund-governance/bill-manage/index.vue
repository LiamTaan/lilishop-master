<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  getBillDetail,
  getBillPage,
  getBillStoreFlowPage,
  payBill
} from "@/api/fund-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getBillStatusLabel,
  getBillStatusType
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "BillManage"
});

const rows = ref<Record<string, any>[]>([]);
const flowRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "结算单号", prop: "sn", minWidth: 180 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "结算金额", prop: "billPriceDisplay", minWidth: 120 },
  { label: "账期", prop: "billRange", minWidth: 220 },
  { label: "状态", prop: "billStatusLabel", minWidth: 120 },
  { label: "出账时间", prop: "createTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 260, slot: "operation" }
];

const flowColumns: TableColumnList = [
  { label: "流水单号", prop: "sn", minWidth: 180 },
  { label: "流水类型", prop: "flowType", minWidth: 120 },
  { label: "支付方式", prop: "paymentName", minWidth: 140 },
  { label: "金额", prop: "price", minWidth: 120 },
  { label: "创建时间", prop: "createTime", minWidth: 180 }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.sn).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.billStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  { label: "结算单数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "待付款单",
    value: filteredRows.value.filter(item => String(item.billStatus).toUpperCase() !== "PAY").length,
    accent: "blue" as const,
    hint: "待完成结算"
  },
  {
    label: "结算总额",
    value: filteredRows.value.reduce((sum, item) => sum + Number(item.billPrice || 0), 0).toFixed(2),
    accent: "green" as const,
    hint: "当前结果金额汇总"
  },
  {
    label: "菜单说明",
    value: "前端挂资金域",
    accent: "purple" as const,
    hint: "后端路径仍位于 order 域"
  }
]);

function normalizeRow(item: Record<string, any>) {
  const billStatus = item.billStatus || item.status || "-";
  return {
    ...item,
    id: item.id || item.billId,
    sn: item.sn || "-",
    storeName: item.storeName || "-",
    billStatus,
    billStatusLabel: getBillStatusLabel(billStatus),
    billStatusType: getBillStatusType(billStatus),
    billPrice: Number(item.billPrice ?? 0),
    billPriceDisplay: `¥ ${Number(item.billPrice ?? 0).toFixed(2)}`,
    billRange: `${formatAdminDateTime(item.startTime)} 至 ${formatAdminDateTime(item.endTime)}`,
    createTime: formatAdminDateTime(item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getBillPage({
      pageNumber: 1,
      pageSize: 200,
      sn: query.keyword || undefined,
      billStatus: query.status || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("结算单列表加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  query.status = payload.status || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  loadData();
}

async function openDetail(row: Record<string, any>) {
  try {
    const [detailRes, flowRes] = await Promise.all([
      getBillDetail(String(row.id)),
      getBillStoreFlowPage(String(row.id), { pageNumber: 1, pageSize: 100 })
    ]);
    currentRow.value = normalizeRow(extractApiPayload(detailRes) || row);
    flowRows.value = extractApiRecords(flowRes).map(item => ({
      ...item,
      sn: item.sn || item.id || "-",
      flowType: item.flowType || "-",
      paymentName: item.paymentName || "-",
      price: `¥ ${Number(item.price ?? item.flowPrice ?? 0).toFixed(2)}`,
      createTime: formatAdminDateTime(item.createTime)
    }));
    detailVisible.value = true;
  } catch (_error) {
    message("结算单详情加载失败", { type: "error" });
  }
}

async function handlePay(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认支付结算单「${row.sn}」吗？`, "支付确认", {
    type: "warning"
  });
  try {
    await payBill(String(row.id));
    message("结算单支付成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("结算单支付失败", { type: "error" });
  }
}

function handleDownload(row: Record<string, any>) {
  window.open(`/manager/order/bill/downLoad/${row.id}`, "_blank");
}

function exportBills() {
  if (!filteredRows.value.length) {
    message("暂无可导出的结算单数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    结算单号: item.sn,
    店铺名称: item.storeName,
    结算金额: item.billPriceDisplay,
    账期: item.billRange,
    状态: item.billStatusLabel,
    出账时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "结算单");
  writeFile(workbook, "结算单.xlsx");
  message("结算单导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="结算单"
    description="承接供货商结算单查看、资金流水核对和付款动作。后端路径位于 order 域，前端菜单挂在资金治理。"
    api-path="/manager/order/bill/getByPage"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '已出账', value: 'OUT' },
      { label: '已核对', value: 'RECON' },
      { label: '已审核', value: 'PASS' },
      { label: '已付款', value: 'PAY' }
    ]"
    keyword-label="结算单号"
    keyword-placeholder="请输入结算单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportBills">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button
        v-if="String(row.billStatus).toUpperCase() !== 'PAY'"
        link
        type="success"
        @click="handlePay(row)"
      >
        支付
      </el-button>
      <el-button link type="warning" @click="handleDownload(row)">下载</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="结算单详情" size="60%">
    <el-descriptions v-if="currentRow" :column="2" border class="mb-4">
      <el-descriptions-item label="结算单号">
        {{ currentRow.sn }}
      </el-descriptions-item>
      <el-descriptions-item label="店铺名称">
        {{ currentRow.storeName }}
      </el-descriptions-item>
      <el-descriptions-item label="结算状态">
        <el-tag :type="currentRow.billStatusType" effect="light" round>
          {{ currentRow.billStatusLabel }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="结算金额">
        {{ currentRow.billPriceDisplay }}
      </el-descriptions-item>
      <el-descriptions-item label="账期" :span="2">
        {{ currentRow.billRange }}
      </el-descriptions-item>
      <el-descriptions-item label="出账时间" :span="2">
        {{ currentRow.createTime }}
      </el-descriptions-item>
    </el-descriptions>

    <pure-table row-key="sn" :data="flowRows" :columns="flowColumns" />
  </el-drawer>
</template>
