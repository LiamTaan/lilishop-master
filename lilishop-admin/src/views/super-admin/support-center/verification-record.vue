<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  getVerificationExceptionPage,
  getVerificationExceptionSummary,
  getVerificationRecordPage,
  getVerificationRecordSummary
} from "@/api/super-admin";
import {
  formatAdminDateTime,
  getVerifyStatusLabel,
  extractApiPayload,
  extractApiRecords
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

const activeView = ref<"normal" | "exception">("normal");
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const rows = ref<Record<string, any>[]>([]);
const exceptionRows = ref<Record<string, any>[]>([]);
const currentSummary = ref<Record<string, any>>({});
const exceptionSummary = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "订单号", prop: "displayName", minWidth: 220 },
  { label: "核销状态", prop: "displayStatus", minWidth: 120 },
  { label: "核销时间", prop: "displayTime", minWidth: 180 },
  { label: "门店/核销员", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 120, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => {
  const source =
    activeView.value === "normal" ? currentSummary.value : exceptionSummary.value;
  return [
    {
      label: activeView.value === "normal" ? "核销总量" : "异常总量",
      value: source.total || rows.value.length,
      accent: "orange" as const,
      hint: activeView.value === "normal" ? "当前核销记录总量" : "当前异常记录总量"
    },
    {
      label: activeView.value === "normal" ? "已核销" : "待处理异常",
      value:
        source.successCount ||
        source.verifiedCount ||
        source.pendingCount ||
        source.exceptionCount ||
        0,
      accent: "green" as const,
      hint: "取后端汇总字段优先展示"
    },
    {
      label: activeView.value === "normal" ? "异常数" : "异常明细",
      value:
        source.exceptionCount ||
        source.abnormalCount ||
        exceptionRows.value.length ||
        0,
      accent: "blue" as const,
      hint: "承接异常治理接口"
    },
    {
      label: "当前视图",
      value: activeView.value === "normal" ? "正常核销" : "异常核销",
      accent: "purple" as const,
      hint: "切换核销治理台账"
    }
  ];
});

const displayRows = computed(() =>
  activeView.value === "normal" ? rows.value : exceptionRows.value
);

function normalizeRecord(item: Record<string, any>, index: number) {
  return {
    ...item,
    id:
      item.id ||
      item.orderSn ||
      item.verificationCode ||
      item.sn ||
      `verification-${index}`,
    displayName: item.orderSn || item.verificationCode || item.goodsName || "-",
    displayStatus:
      getVerifyStatusLabel(
        item.resultType ||
          item.verifyStatus ||
          item.status ||
          item.exceptionStatus ||
          item.verificationStatus ||
          "-"
      ),
    displayTime: formatAdminDateTime(
      item.verifyTime || item.createTime || item.updateTime || item.finishTime || "-"
    ),
    displayRemark:
      [item.storeName, item.operatorName, item.memberName]
        .filter(Boolean)
        .join(" / ") || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) {
      params.keyword = query.keyword;
      params.orderSn = query.keyword;
    }
    if (query.status) {
      params.status = query.status;
      params.resultType = query.status;
    }
    const [
      normalPageRes,
      normalSummaryRes,
      exceptionPageRes,
      exceptionSummaryRes
    ] = await Promise.all([
      getVerificationRecordPage(params),
      getVerificationRecordSummary(params),
      getVerificationExceptionPage(params),
      getVerificationExceptionSummary(params)
    ]);
    rows.value = extractApiRecords(normalPageRes).map(normalizeRecord);
    currentSummary.value = extractApiPayload<Record<string, any>>(normalSummaryRes) || {};
    exceptionRows.value = extractApiRecords(exceptionPageRes).map(normalizeRecord);
    exceptionSummary.value =
      extractApiPayload<Record<string, any>>(exceptionSummaryRes) || {};
  } catch (_error) {
    message("核销记录加载失败，请稍后重试", { type: "error" });
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

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="核销记录"
    description="承接正常核销与异常核销台账切换、核销汇总和详情抽屉，作为平台订单履约中的核销治理页。"
    api-path="/manager/other/verificationRecord"
    :columns="columns"
    :data="displayRows"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '正常核销', value: '已接入', type: 'primary' },
      { label: '异常核销', value: '已接入', type: 'warning' },
      { label: '详情查看', value: '抽屉承接', type: 'success' }
    ]"
    keyword-label="订单/核销码"
    keyword-placeholder="请输入订单号或核销码"
    :status-options="[
      { label: '核销成功', value: 'SUCCESS' },
      { label: '核销失败', value: 'FAIL' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-radio-group v-model="activeView" @change="loadData">
        <el-radio-button label="normal">正常核销</el-radio-button>
        <el-radio-button label="exception">异常核销</el-radio-button>
      </el-radio-group>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="核销详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="订单号">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="核销状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="核销时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="门店/核销员">
        {{ currentRow.displayRemark }}
      </el-descriptions-item>
      <el-descriptions-item label="原始数据">
        <pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}
</style>
