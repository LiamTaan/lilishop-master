<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  createCouponActivity,
  deleteCouponActivity,
  getCouponActivityDetail,
  getCouponActivityPage
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
  name: "CouponActivityManage"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const payloadText = ref("");
const saving = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "活动类型", prop: "couponActivityType", minWidth: 140 },
  { label: "活动范围", prop: "activityScope", minWidth: 140 },
  { label: "领取周期", prop: "couponFrequencyEnum", minWidth: 120 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
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

function defaultPayload() {
  return {
    promotionName: "",
    couponActivityType: "SPECIFY",
    activityScope: "ALL",
    couponFrequencyEnum: "DAY",
    activityScopeInfo: "",
    startTime: "",
    endTime: "",
    couponActivityItems: [],
    memberDTOS: []
  };
}

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.couponActivityId,
    promotionName: item.promotionName || "-",
    couponActivityType: item.couponActivityType || "-",
    activityScope: item.activityScope || "-",
    couponFrequencyEnum: item.couponFrequencyEnum || "-",
    promotionStatus: item.promotionStatus || item.status || "-",
    promotionStatusLabel: getPromotionStatusLabel(item.promotionStatus || item.status || "-"),
    createTime: formatAdminDateTime(item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getCouponActivityPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("券活动列表加载失败", { type: "error" });
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

function openCreate() {
  payloadText.value = JSON.stringify(defaultPayload(), null, 2);
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getCouponActivityDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("券活动详情加载失败", { type: "error" });
  }
}

async function submitPayload() {
  let payload: Record<string, any>;
  try {
    payload = JSON.parse(payloadText.value || "{}");
  } catch (_error) {
    message("JSON 格式不正确", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    await createCouponActivity(payload);
    message("券活动创建成功", { type: "success" });
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("券活动创建失败，请检查 JSON 载荷", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认关闭券活动「${row.promotionName}」吗？`, "关闭确认", {
    type: "warning"
  });
  try {
    await deleteCouponActivity(String(row.id));
    message("券活动已关闭", { type: "success" });
    await loadData();
  } catch (_error) {
    message("券活动关闭失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="券活动管理"
    description="承接优惠券活动的列表、详情、新增和关闭处理。"
    api-path="/manager/promotion/couponActivity"
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
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增券活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleDelete(row)">关闭</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" title="新增券活动 JSON" width="860px">
    <el-input v-model="payloadText" type="textarea" :rows="24" />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="券活动详情" size="46%">
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
