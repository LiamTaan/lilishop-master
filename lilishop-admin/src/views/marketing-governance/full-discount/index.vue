<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import {
  getFullDiscountDetail,
  getFullDiscountPage,
  updateFullDiscountStatus
} from "@/api/marketing-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import {
  formatBooleanLabel,
  formatMoney,
  normalizePromotionDetail,
  toTimeValue
} from "../shared/promotion-helpers";

defineOptions({
  name: "FullDiscountManage"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const statusVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const statusForm = reactive({
  startTime: "",
  endTime: ""
});

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 200 },
  { label: "门槛金额", prop: "fullMoneyDisplay", minWidth: 120 },
  { label: "优惠内容", prop: "benefitText", minWidth: 220, showOverflowTooltip: true },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "活动时间", prop: "activityTime", minWidth: 240 },
  { label: "操作", prop: "operation", fixed: "right", width: 220, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.promotionName).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  {
    label: "活动总数",
    value: filteredRows.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "进行中",
    value: filteredRows.value.filter(item => String(item.promotionStatus).toUpperCase() === "START").length,
    accent: "green" as const,
    hint: "当前有效满减"
  },
  {
    label: "包邮活动",
    value: filteredRows.value.filter(item => Boolean(item.freeFreightFlag)).length,
    accent: "blue" as const,
    hint: "当前含包邮权益"
  },
  {
    label: "治理动作",
    value: "详情/状态/导出",
    accent: "purple" as const,
    hint: "已接入真实导出"
  }
]);

const detailFields = computed(() => {
  if (!currentRow.value) {
    return [];
  }
  const detail = currentRow.value;
  return [
    { label: "活动名称", value: detail.promotionName || detail.title || "-" },
    { label: "门槛金额", value: formatMoney(detail.fullMoney) },
    { label: "减现金", value: detail.fullMinusFlag ? formatMoney(detail.fullMinus) : "未启用" },
    { label: "打折", value: detail.fullRateFlag ? `${detail.fullRate || 0} 折` : "未启用" },
    { label: "包邮", value: formatBooleanLabel(detail.freeFreightFlag) },
    { label: "赠积分", value: detail.pointFlag ? `${detail.point || 0} 积分` : "未启用" },
    { label: "赠优惠券", value: detail.couponFlag ? detail.couponId || "已配置" : "未启用" },
    { label: "赠品", value: detail.giftFlag ? detail.giftSkuName || detail.giftId || "已配置" : "未启用" },
    { label: "开始时间", value: detail.startTimeText || "-" },
    { label: "结束时间", value: detail.endTimeText || "-" },
    { label: "状态", value: detail.promotionStatusLabel || "-" },
    { label: "活动说明", value: detail.description || "-" }
  ];
});

function normalizeRow(item: Record<string, any>) {
  return {
    ...normalizePromotionDetail(item),
    promotionName: item.promotionName || item.title || "-",
    fullMoneyDisplay: `¥ ${Number(item.fullMoney ?? item.consumeThreshold ?? 0).toFixed(2)}`,
    benefitText:
      item.benefitText ||
      [
        item.fullMinusFlag ? `减 ${item.fullMinus}` : "",
        item.fullRateFlag ? `打 ${item.fullRate} 折` : "",
        item.freeFreightFlag ? "包邮" : "",
        item.pointFlag ? `送 ${item.point} 积分` : "",
        item.couponFlag ? "送优惠券" : "",
        item.giftFlag ? `送赠品 ${item.giftSkuName || item.giftId || ""}` : ""
      ]
        .filter(Boolean)
        .join(" / ") ||
      "-",
    activityTime: `${formatAdminDateTime(item.startTime)} 至 ${formatAdminDateTime(item.endTime)}`
  };
}

async function loadData() {
  try {
    const response = await getFullDiscountPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("满减活动列表加载失败", { type: "error" });
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
    const response = await getFullDiscountDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("满减活动详情加载失败", { type: "error" });
  }
}

function openStatusDialog(row: Record<string, any>) {
  currentRow.value = row;
  statusForm.startTime = "";
  statusForm.endTime = "";
  statusVisible.value = true;
}

async function submitStatus() {
  if (!currentRow.value) return;
  try {
    await updateFullDiscountStatus(String(currentRow.value.id), {
      startTime: toTimeValue(statusForm.startTime),
      endTime: toTimeValue(statusForm.endTime)
    });
    message("满减活动状态更新成功", { type: "success" });
    statusVisible.value = false;
    await loadData();
  } catch (_error) {
    message("满减活动状态更新失败", { type: "error" });
  }
}

function exportFullDiscount() {
  if (!filteredRows.value.length) {
    message("暂无可导出的满减活动数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    活动名称: item.promotionName,
    门槛金额: item.fullMoneyDisplay,
    优惠内容: item.benefitText,
    状态: item.promotionStatusLabel || getPromotionStatusLabel(item.promotionStatus),
    活动时间: item.activityTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "满减活动管理");
  writeFile(workbook, "满减活动管理.xlsx");
  message("满减活动数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="满减活动管理"
    description="承接满减活动列表、详情和状态调整，确保平台满减可从管理端落地。"
    api-path="/manager/promotion/fullDiscount"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' }
    ]"
    keyword-label="活动名称"
    keyword-placeholder="请输入活动名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportFullDiscount">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="warning" @click="openStatusDialog(row)">状态</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="statusVisible" title="满减活动状态调整" width="520px">
    <el-form label-width="92px">
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="statusForm.startTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="statusForm.endTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="statusVisible = false">取消</el-button>
      <el-button type="primary" @click="submitStatus">提交</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="满减活动详情" size="56%">
    <el-descriptions v-if="currentRow" :column="2" border>
      <el-descriptions-item
        v-for="field in detailFields"
        :key="field.label"
        :label="field.label"
        :span="field.label === '活动说明' ? 2 : 1"
      >
        <div class="detail-text">{{ field.value }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
