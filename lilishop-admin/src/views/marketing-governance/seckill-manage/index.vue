<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  deleteSeckill,
  deleteSeckillApply,
  getSeckillApplyPage,
  getSeckillDetail,
  getSeckillInit,
  getSeckillPage,
  updateSeckill,
  updateSeckillStatus
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
  name: "SeckillManage"
});

const rows = ref<Record<string, any>[]>([]);
const applyRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const applyVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const currentInit = ref<Record<string, any>>({});
const payloadText = ref("");
const saving = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "报名截止", prop: "applyEndTime", minWidth: 180 },
  { label: "场次", prop: "hours", minWidth: 140 },
  { label: "商品数", prop: "goodsNum", minWidth: 100 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 320, slot: "operation" }
];

const applyColumns: TableColumnList = [
  { label: "报名ID", prop: "id", minWidth: 160 },
  { label: "商品名称", prop: "goodsName", minWidth: 220 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "报名状态", prop: "applyStatus", minWidth: 120 },
  { label: "创建时间", prop: "createTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 120, slot: "operation" }
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
    id: item.id || item.seckillId,
    promotionName: item.promotionName || "-",
    applyEndTime: formatAdminDateTime(item.applyEndTime),
    hours: item.hours || "-",
    goodsNum: Number(item.goodsNum ?? 0),
    promotionStatus: item.promotionStatus || item.status || "-",
    promotionStatusLabel: getPromotionStatusLabel(item.promotionStatus || item.status || "-")
  };
}

async function loadData() {
  try {
    const [listRes, initRes] = await Promise.all([
      getSeckillPage({
        pageNumber: 1,
        pageSize: 200,
        promotionName: query.keyword || undefined
      }),
      getSeckillInit()
    ]);
    rows.value = extractApiRecords(listRes).map(normalizeRow);
    currentInit.value = extractApiPayload<Record<string, any>>(initRes) || {};
  } catch (_error) {
    rows.value = [];
    currentInit.value = {};
    message("秒杀活动加载失败", { type: "error" });
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

function openCreateOrInit() {
  payloadText.value = JSON.stringify(currentInit.value || {}, null, 2);
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getSeckillDetail(String(row.id));
    payloadText.value = JSON.stringify(extractApiPayload(response) || row, null, 2);
    dialogVisible.value = true;
  } catch (_error) {
    message("秒杀活动详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getSeckillDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("秒杀活动详情加载失败", { type: "error" });
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
    await updateSeckill(payload);
    message("秒杀活动保存成功", { type: "success" });
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("秒杀活动保存失败，请检查 JSON 载荷", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除秒杀活动「${row.promotionName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteSeckill(String(row.id));
    message("秒杀活动删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("秒杀活动删除失败", { type: "error" });
  }
}

async function handleStatus(row: Record<string, any>) {
  try {
    await updateSeckillStatus(String(row.id));
    message("秒杀活动状态更新成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("秒杀活动状态更新失败", { type: "error" });
  }
}

async function openApply(row: Record<string, any>) {
  currentRow.value = row;
  try {
    const response = await getSeckillApplyPage({
      seckillId: row.id,
      pageNumber: 1,
      pageSize: 200
    });
    applyRows.value = extractApiRecords(response).map(item => ({
      ...item,
      id: item.id || item.applyId,
      goodsName: item.goodsName || item.skuName || "-",
      storeName: item.storeName || "-",
      applyStatus: item.applyStatus || item.status || "-",
      createTime: formatAdminDateTime(item.createTime)
    }));
    applyVisible.value = true;
  } catch (_error) {
    applyRows.value = [];
    message("秒杀报名商品加载失败", { type: "error" });
  }
}

async function handleDeleteApply(row: Record<string, any>) {
  if (!currentRow.value) return;
  try {
    await deleteSeckillApply(String(currentRow.value.id), String(row.id));
    message("报名商品移除成功", { type: "success" });
    await openApply(currentRow.value);
  } catch (_error) {
    message("报名商品移除失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="秒杀活动管理"
    description="承接秒杀活动列表、详情、保存、状态切换和报名商品治理。"
    api-path="/manager/promotion/seckill"
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
      <el-button type="primary" @click="openCreateOrInit">保存秒杀活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="handleStatus(row)">状态</el-button>
      <el-button link type="primary" @click="openApply(row)">报名商品</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" title="秒杀活动 JSON" width="860px">
    <el-input v-model="payloadText" type="textarea" :rows="24" />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="秒杀活动详情" size="46%">
    <pre v-if="currentRow" class="json-panel">{{ JSON.stringify(currentRow, null, 2) }}</pre>
  </el-drawer>

  <el-dialog v-model="applyVisible" title="秒杀报名商品" width="980px">
    <pure-table row-key="id" :data="applyRows" :columns="applyColumns">
      <template #operation="{ row }">
        <el-button link type="danger" @click="handleDeleteApply(row)">移除</el-button>
      </template>
    </pure-table>
  </el-dialog>
</template>

<style scoped>
.json-panel {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
