<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  createKanjia,
  deleteKanjia,
  getKanjiaDetail,
  getKanjiaPage,
  updateKanjia
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
  name: "KanjiaManage"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const payloadText = ref("");
const saving = ref(false);
const editingId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "商品名称", prop: "goodsName", minWidth: 220 },
  { label: "活动库存", prop: "stock", minWidth: 100 },
  { label: "结算价", prop: "settlementPriceDisplay", minWidth: 120 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 260, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? [item.promotionName, item.goodsName].some(value =>
          String(value || "").includes(query.keyword)
        )
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

function defaultCreatePayload() {
  return {
    startTime: "",
    endTime: "",
    promotionGoodsList: []
  };
}

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.promotionId,
    promotionName: item.promotionName || "-",
    goodsName: item.goodsName || "-",
    stock: Number(item.stock ?? 0),
    settlementPriceDisplay: `¥ ${Number(item.settlementPrice ?? 0).toFixed(2)}`,
    promotionStatus: item.promotionStatus || item.status || "-",
    promotionStatusLabel: getPromotionStatusLabel(item.promotionStatus || item.status || "-"),
    startTime: formatAdminDateTime(item.startTime),
    endTime: formatAdminDateTime(item.endTime)
  };
}

async function loadData() {
  try {
    const response = await getKanjiaPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("砍价活动列表加载失败", { type: "error" });
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
  payloadText.value = JSON.stringify(defaultCreatePayload(), null, 2);
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getKanjiaDetail(String(row.id));
    editingId.value = String(row.id);
    payloadText.value = JSON.stringify(extractApiPayload(response) || row, null, 2);
    dialogVisible.value = true;
  } catch (_error) {
    message("砍价活动详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getKanjiaDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("砍价活动详情加载失败", { type: "error" });
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
    if (editingId.value) {
      payload.id = editingId.value;
      await updateKanjia(payload);
      message("砍价活动更新成功", { type: "success" });
    } else {
      await createKanjia(payload);
      message("砍价活动创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("砍价活动保存失败，请检查 JSON 载荷", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除砍价活动「${row.promotionName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteKanjia([String(row.id)]);
    message("砍价活动删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("砍价活动删除失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="砍价活动管理"
    description="承接砍价活动列表、详情、创建、编辑和删除。"
    api-path="/manager/promotion/kanJiaGoods"
    :columns="columns"
    :data="filteredRows"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' }
    ]"
    keyword-label="活动/商品"
    keyword-placeholder="请输入活动名称或商品名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增砍价活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑砍价活动 JSON' : '新增砍价活动 JSON'"
    width="860px"
  >
    <el-input v-model="payloadText" type="textarea" :rows="24" />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="砍价活动详情" size="46%">
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
