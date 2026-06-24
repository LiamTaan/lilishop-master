<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  createCoupon,
  deleteCoupons,
  getCouponDetail,
  getCouponMembers,
  getCouponPage,
  updateCoupon,
  updateCouponStatus
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
  name: "CouponManage"
});

const rows = ref<Record<string, any>[]>([]);
const memberRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const memberVisible = ref(false);
const statusVisible = ref(false);
const saving = ref(false);
const editingId = ref("");
const currentRow = ref<Record<string, any> | null>(null);
const payloadText = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const statusForm = reactive({
  startTime: "",
  endTime: ""
});

const columns: TableColumnList = [
  { label: "优惠券名称", prop: "couponName", minWidth: 180 },
  { label: "活动名称", prop: "promotionName", minWidth: 180 },
  { label: "优惠类型", prop: "couponType", minWidth: 120 },
  { label: "面额/折扣", prop: "amountDisplay", minWidth: 120 },
  { label: "发放数量", prop: "publishNum", minWidth: 100 },
  { label: "已领/已用", prop: "receivedUsedText", minWidth: 120 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 320, slot: "operation" }
];

const memberColumns: TableColumnList = [
  { label: "会员", prop: "memberName", minWidth: 160 },
  { label: "领取状态", prop: "memberStatus", minWidth: 120 },
  { label: "领取时间", prop: "createTime", minWidth: 180 },
  { label: "使用时间", prop: "usedTime", minWidth: 180 }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? [item.couponName, item.promotionName].some(value =>
          String(value || "").includes(query.keyword)
        )
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  { label: "优惠券数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "进行中",
    value: filteredRows.value.filter(item => String(item.promotionStatus).toUpperCase() === "START").length,
    accent: "green" as const,
    hint: "当前可领取"
  },
  {
    label: "已领数量",
    value: filteredRows.value.reduce((sum, item) => sum + Number(item.receivedNum || 0), 0),
    accent: "blue" as const,
    hint: "累计已领取"
  },
  { label: "治理动作", value: "增改删/状态", accent: "purple" as const, hint: "已接入真实接口" }
]);

function defaultPayload() {
  return {
    promotionName: "平台优惠券",
    couponName: "",
    couponType: "PRICE",
    price: 10,
    couponDiscount: 0,
    getType: "FREE_GET",
    publishNum: 100,
    couponLimitNum: 1,
    consumeThreshold: 0,
    description: "",
    rangeDayType: "FIXEDTIME",
    startTime: "",
    endTime: "",
    promotionGoodsList: []
  };
}

function normalizeRow(item: Record<string, any>) {
  const price = Number(item.price ?? 0);
  const couponDiscount = Number(item.couponDiscount ?? 0);
  return {
    ...item,
    id: item.id || item.couponId,
    promotionName: item.promotionName || item.couponName || "-",
    couponName: item.couponName || "-",
    couponType: item.couponTypeLabel || item.couponType || "-",
    amountDisplay:
      item.faceValueText ||
      (item.couponType === "DISCOUNT" ? `${couponDiscount} 折` : `¥ ${price.toFixed(2)}`),
    publishNum: Number(item.publishNum ?? 0),
    receivedNum: Number(item.receivedNum ?? 0),
    usedNum: Number(item.usedNum ?? 0),
    receivedUsedText: `${Number(item.receivedNum ?? 0)} / ${Number(item.usedNum ?? 0)}`,
    promotionStatus: item.promotionStatus || item.status || "-",
    promotionStatusLabel:
      item.promotionStatusLabel ||
      getPromotionStatusLabel(item.promotionStatus || item.status || "-"),
    startTime: formatAdminDateTime(item.startTime),
    endTime: formatAdminDateTime(item.endTime),
    createTime: formatAdminDateTime(item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getCouponPage({
      pageNumber: 1,
      pageSize: 200,
      couponName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("优惠券列表加载失败", { type: "error" });
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
  editingId.value = "";
  payloadText.value = JSON.stringify(defaultPayload(), null, 2);
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getCouponDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    editingId.value = String(detail.id || row.id);
    payloadText.value = JSON.stringify(detail, null, 2);
    dialogVisible.value = true;
  } catch (_error) {
    message("优惠券详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getCouponDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("优惠券详情加载失败", { type: "error" });
  }
}

async function openMembers(row: Record<string, any>) {
  try {
    const response = await getCouponMembers(String(row.id), {
      pageNumber: 1,
      pageSize: 200
    });
    memberRows.value = extractApiRecords(response).map(item => ({
      ...item,
      id: item.id || item.memberCouponId,
      memberName: item.memberName || item.nickname || item.memberId || "-",
      memberStatus: item.memberCouponStatus || item.status || "-",
      createTime: formatAdminDateTime(item.createTime),
      usedTime: formatAdminDateTime(item.usedTime)
    }));
    memberVisible.value = true;
  } catch (_error) {
    memberRows.value = [];
    message("优惠券领取明细加载失败", { type: "error" });
  }
}

async function submitPayload() {
  let payload: Record<string, any>;
  try {
    payload = JSON.parse(payloadText.value || "{}");
  } catch (_error) {
    message("JSON 格式不正确，请检查后重试", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    if (editingId.value) {
      payload.id = editingId.value;
      await updateCoupon(payload);
      message("优惠券更新成功", { type: "success" });
    } else {
      await createCoupon(payload);
      message("优惠券创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("优惠券保存失败，请检查 JSON 载荷", { type: "error" });
  } finally {
    saving.value = false;
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
    await updateCouponStatus({
      couponIds: currentRow.value.id,
      startTime: statusForm.startTime ? new Date(statusForm.startTime).getTime() : undefined,
      endTime: statusForm.endTime ? new Date(statusForm.endTime).getTime() : undefined
    });
    message("优惠券状态更新成功", { type: "success" });
    statusVisible.value = false;
    await loadData();
  } catch (_error) {
    message("优惠券状态更新失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除优惠券「${row.couponName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteCoupons([String(row.id)]);
    message("优惠券删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("优惠券删除失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="优惠券管理"
    description="承接平台优惠券的列表、详情、状态、删除和领取明细查看。"
    api-path="/manager/promotion/coupon"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' }
    ]"
    keyword-label="优惠券"
    keyword-placeholder="请输入优惠券名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="openStatusDialog(row)">状态</el-button>
      <el-button link type="primary" @click="openMembers(row)">领取明细</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑优惠券 JSON' : '新增优惠券 JSON'"
    width="860px"
  >
    <el-alert
      title="优惠券结构较复杂，当前页面使用 JSON 载荷表单承接创建和编辑。"
      type="info"
      :closable="false"
      class="mb-4"
    />
    <el-input v-model="payloadText" type="textarea" :rows="24" />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="statusVisible" title="优惠券状态调整" width="520px">
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

  <el-drawer v-model="detailVisible" title="优惠券详情" size="46%">
    <pre v-if="currentRow" class="json-panel">{{ JSON.stringify(currentRow, null, 2) }}</pre>
  </el-drawer>

  <el-dialog v-model="memberVisible" title="优惠券领取明细" width="860px">
    <pure-table row-key="id" :data="memberRows" :columns="memberColumns" />
  </el-dialog>
</template>

<style scoped>
.json-panel {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #4b515c;
}
</style>
