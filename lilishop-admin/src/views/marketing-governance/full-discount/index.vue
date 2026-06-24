<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
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

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.promotionId,
    promotionName: item.promotionName || "-",
    promotionStatus: item.promotionStatus || item.status || "-",
    promotionStatusLabel: getPromotionStatusLabel(item.promotionStatus || item.status || "-"),
    fullMoneyDisplay: `¥ ${Number(item.fullMoney ?? item.consumeThreshold ?? 0).toFixed(2)}`,
    benefitText:
      item.benefitText ||
      [
        item.reducePrice ? `减 ${item.reducePrice}` : "",
        item.discount ? `折扣 ${item.discount}` : "",
        item.freeFreight ? "包邮" : ""
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
      startTime: statusForm.startTime ? new Date(statusForm.startTime).getTime() : undefined,
      endTime: statusForm.endTime ? new Date(statusForm.endTime).getTime() : undefined
    });
    message("满减活动状态更新成功", { type: "success" });
    statusVisible.value = false;
    await loadData();
  } catch (_error) {
    message("满减活动状态更新失败", { type: "error" });
  }
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

  <el-drawer v-model="detailVisible" title="满减活动详情" size="46%">
    <pre v-if="currentRow" class="json-panel">{{ JSON.stringify(currentRow, null, 2) }}</pre>
  </el-drawer>
</template>

<style scoped>
.json-panel {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
