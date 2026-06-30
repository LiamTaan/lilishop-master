<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
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
import {
  formatDateTimeForField,
  normalizePromotionDetail,
  toTimeValue
} from "../shared/promotion-helpers";

defineOptions({
  name: "SeckillManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const applyRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const applyVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const saving = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});
const seckillForm = reactive(createSeckillForm());

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

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
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
    hint: "当前有效秒杀"
  },
  {
    label: "报名商品",
    value: filteredRows.value.reduce((sum, item) => sum + Number(item.goodsNum || 0), 0),
    accent: "blue" as const,
    hint: "当前活动商品总数"
  },
  {
    label: "治理动作",
    value: "编辑/删/导出",
    accent: "purple" as const,
    hint: "已接入批量删除"
  }
]);

const detailFields = computed(() => {
  if (!currentRow.value) {
    return [];
  }
  const detail = currentRow.value;
  return [
    { label: "活动名称", value: detail.promotionName || "-" },
    { label: "报名截止", value: formatAdminDateTime(detail.applyEndTime) },
    { label: "活动日期", value: detail.startTimeText || "-" },
    { label: "结束时间", value: detail.endTimeText || "-" },
    { label: "秒杀场次", value: detail.hours || "-" },
    { label: "秒杀规则", value: detail.seckillRule || "-" },
    { label: "已报名商品", value: detail.goodsNum || 0 },
    { label: "状态", value: detail.promotionStatusLabel || "-" },
    { label: "参与店铺", value: detail.storeIds || "-" }
  ];
});

function createSeckillForm() {
  return {
    id: "",
    promotionName: "",
    startTime: "",
    endTime: "",
    applyEndTime: "",
    hours: "",
    seckillRule: ""
  };
}

function fillSeckillForm(source: Record<string, any>) {
  Object.assign(seckillForm, {
    id: source.id || "",
    promotionName: source.promotionName || "",
    startTime: formatDateTimeForField(source.startTime),
    endTime: formatDateTimeForField(source.endTime),
    applyEndTime: formatDateTimeForField(source.applyEndTime),
    hours: source.hours || "",
    seckillRule: source.seckillRule || ""
  });
}

function buildSeckillPayload() {
  return {
    ...seckillForm,
    id: seckillForm.id,
    promotionName: seckillForm.promotionName,
    startTime: seckillForm.startTime,
    endTime: seckillForm.endTime,
    applyEndTime: seckillForm.applyEndTime,
    hours: seckillForm.hours,
    seckillRule: seckillForm.seckillRule
  };
}

function normalizeRow(item: Record<string, any>) {
  return {
    ...normalizePromotionDetail(item),
    promotionName: item.promotionName || "-",
    applyEndTime: formatAdminDateTime(item.applyEndTime),
    hours: item.hours || "-",
    goodsNum: Number(item.goodsNum ?? 0)
  };
}

async function loadData() {
  try {
    const listRes = await getSeckillPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(listRes).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

async function initializeSeckill() {
  try {
    await getSeckillInit();
    message("秒杀活动已按系统配置完成初始化", { type: "success" });
    await loadData();
  } catch (_error) {
    message("秒杀活动初始化失败", { type: "error" });
  }
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getSeckillDetail(String(row.id));
    fillSeckillForm(extractApiPayload(response) || row);
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
  saving.value = true;
  try {
    await updateSeckill(buildSeckillPayload());
    message("秒杀活动保存成功", { type: "success" });
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("秒杀活动保存失败，请检查表单内容", { type: "error" });
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

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的秒杀活动", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个秒杀活动吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteSeckill(id)));
    selectedRows.value = [];
    message("秒杀活动批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("秒杀活动批量删除失败", { type: "error" });
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

function exportSeckill() {
  if (!filteredRows.value.length) {
    message("暂无可导出的秒杀活动数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    活动名称: item.promotionName,
    报名截止: item.applyEndTime,
    秒杀场次: item.hours,
    商品数: item.goodsNum,
    状态: item.promotionStatusLabel || getPromotionStatusLabel(item.promotionStatus),
    开始时间: item.startTimeText || "-",
    结束时间: item.endTimeText || "-"
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "秒杀活动管理");
  writeFile(workbook, "秒杀活动管理.xlsx");
  message("秒杀活动数据导出成功", { type: "success" });
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
    selectable
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
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportSeckill">导出</el-button>
      <el-button type="primary" @click="initializeSeckill">初始化秒杀活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="handleStatus(row)">状态</el-button>
      <el-button link type="primary" @click="openApply(row)">报名商品</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" title="编辑秒杀活动" width="820px">
    <el-form label-width="110px">
      <div class="form-grid">
        <el-form-item label="活动名称">
          <el-input v-model="seckillForm.promotionName" />
        </el-form-item>
        <el-form-item label="秒杀场次">
          <el-input v-model="seckillForm.hours" placeholder="例如 9,10,11,12" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="seckillForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="seckillForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报名截止">
          <el-date-picker
            v-model="seckillForm.applyEndTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <el-form-item label="秒杀规则">
        <el-input v-model="seckillForm.seckillRule" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="秒杀活动详情" size="56%">
    <el-descriptions v-if="currentRow" :column="2" border class="mb-4">
      <el-descriptions-item
        v-for="field in detailFields"
        :key="field.label"
        :label="field.label"
        :span="field.label === '秒杀规则' ? 2 : 1"
      >
        <div class="detail-text">{{ field.value }}</div>
      </el-descriptions-item>
    </el-descriptions>
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
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (width <= 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
