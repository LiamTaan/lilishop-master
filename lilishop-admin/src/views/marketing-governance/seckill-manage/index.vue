<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  addSeckillApply,
  deleteSeckill,
  deleteSeckillApply,
  getSeckillAvailableSkuPage,
  getSeckillApplyPage,
  getSeckillDetail,
  getSeckillInit,
  getSeckillPage,
  updateSeckill,
  updateSeckillApply,
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
  formatMoney,
  normalizePromotionDetail,
  toNumber
} from "../shared/promotion-helpers";

defineOptions({
  name: "SeckillManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const applyRows = ref<Record<string, any>[]>([]);
const skuRows = ref<Record<string, any>[]>([]);
const selectedApplyRows = ref<Record<string, any>[]>([]);
const skuTableKey = ref(0);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const applyVisible = ref(false);
const addGoodsVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const saving = ref(false);
const applySaving = ref(false);
const applyRowSavingId = ref("");
const skuLoading = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});
const skuQuery = reactive({
  keyword: "",
  pageNumber: 1,
  pageSize: 10,
  total: 0
});
const applyQuery = reactive({
  keyword: "",
  pageNumber: 1,
  pageSize: 10,
  total: 0
});
const addGoodsForm = reactive({
  timeLine: undefined as number | undefined
});
const seckillForm = reactive(createSeckillForm());

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "报名截止", prop: "applyEndTime", minWidth: 180 },
  { label: "场次", prop: "hours", minWidth: 140 },
  { label: "商品数", prop: "goodsNum", minWidth: 100 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 380, slot: "operation" }
];

const applyColumns: TableColumnList = [
  { label: "报名ID", prop: "id", minWidth: 160 },
  { label: "商品名称", prop: "goodsName", minWidth: 220 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "场次", prop: "timeLineText", minWidth: 90 },
  { label: "秒杀价", prop: "price", minWidth: 150, slot: "price" },
  { label: "活动库存", prop: "quantity", minWidth: 150, slot: "quantity" },
  { label: "报名状态", prop: "applyStatus", minWidth: 120 },
  { label: "创建时间", prop: "createTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 150, slot: "operation" }
];

const skuColumns: TableColumnList = [
  { label: "商品名称", prop: "goodsName", minWidth: 220, showOverflowTooltip: true },
  { label: "规格", prop: "simpleSpecs", minWidth: 160, showOverflowTooltip: true },
  { label: "店铺", prop: "storeName", minWidth: 160, showOverflowTooltip: true },
  { label: "货号", prop: "sn", minWidth: 130, showOverflowTooltip: true },
  { label: "原价", prop: "priceText", minWidth: 100 },
  { label: "商品库存", prop: "stock", minWidth: 100 },
  { label: "秒杀价", prop: "seckillPrice", minWidth: 150, slot: "seckillPrice" },
  { label: "活动库存", prop: "applyQuantity", minWidth: 150, slot: "applyQuantity" }
];

const selectableSkuColumns = computed(() => [
  {
    type: "selection",
    width: 54,
    reserveSelection: true,
    align: "center"
  },
  ...skuColumns
] as TableColumnList);

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

const currentTimeLineOptions = computed(() => {
  const hours = String(currentRow.value?.hours || "")
    .split(",")
    .map(item => Number(item.trim()))
    .filter(item => Number.isFinite(item));
  return Array.from(new Set(hours)).sort((a, b) => a - b);
});

function normalizeSkuRow(item: Record<string, any>) {
  const skuId = String(item.id || item.skuId || "").trim();
  return {
    ...item,
    id: skuId,
    skuId,
    goodsName: item.goodsName || item.name || "-",
    simpleSpecs: item.simpleSpecs || "-",
    storeName: item.storeName || "-",
    sn: item.sn || "-",
    price: toNumber(item.price || item.originalPrice),
    priceText: formatMoney(item.price || item.originalPrice),
    stock: Number(item.quantity ?? item.stock ?? 0),
    seckillPrice: toNumber(item.seckillPrice || item.price || item.originalPrice),
    applyQuantity: Number(item.quantity ?? item.stock ?? 0) > 0 ? 1 : 0
  };
}

async function loadSkuData() {
  if (!currentRow.value) return;
  skuLoading.value = true;
  try {
    const response = await getSeckillAvailableSkuPage(String(currentRow.value.id), {
      pageNumber: skuQuery.pageNumber,
      pageSize: skuQuery.pageSize,
      skuKeyword: skuQuery.keyword.trim() || undefined,
      timeLine: addGoodsForm.timeLine
    });
    skuRows.value = extractApiRecords(response).map(normalizeSkuRow);
    const payload = extractApiPayload<Record<string, any>>(response) || {};
    skuQuery.total = Number(payload.total || payload.totalElements || skuRows.value.length);
  } catch (_error) {
    skuRows.value = [];
    skuQuery.total = 0;
    message("商品搜索失败", { type: "error" });
  } finally {
    skuLoading.value = false;
  }
}

function searchSkuData() {
  resetSkuSelection();
  skuQuery.pageNumber = 1;
  loadSkuData();
}

function handleSkuPageChange(page: number) {
  skuQuery.pageNumber = page;
  loadSkuData();
}

function handleSkuSelectionChange(rows: Record<string, any>[]) {
  const rowMap = new Map<string, Record<string, any>>();
  rows.forEach(item => {
    const skuId = String(item.skuId || item.id || "").trim();
    if (skuId) {
      rowMap.set(skuId, item);
    }
  });
  selectedApplyRows.value = Array.from(rowMap.values());
}

function resetSkuSelection() {
  selectedApplyRows.value = [];
  skuTableKey.value += 1;
}

function handleTimeLineChange() {
  searchSkuData();
}

function normalizeApplyRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.applyId,
    skuId: String(item.skuId || "").trim(),
    goodsName: item.goodsName || item.skuName || "-",
    storeName: item.storeName || "-",
    timeLineText: item.timeLine === undefined || item.timeLine === null ? "-" : `${item.timeLine}:00`,
    price: toNumber(item.price),
    priceText: formatMoney(item.price),
    quantity: Number(item.quantity ?? 0),
    originalPrice: toNumber(item.originalPrice),
    stock: Number(item.stock ?? item.skuQuantity ?? item.goodsQuantity ?? 0),
    applyStatus: item.applyStatus || item.status || "-",
    createTime: formatAdminDateTime(item.createTime)
  };
}

async function loadRegisteredApplies(seckillId: string, options: Record<string, any> = {}) {
  const response = await getSeckillApplyPage({
    seckillId,
    pageNumber: options.pageNumber || 1,
    pageSize: options.pageSize || 10,
    goodsName: options.keyword || undefined
  });
  const payload = extractApiPayload<Record<string, any>>(response) || {};
  return {
    records: extractApiRecords(response).map(normalizeApplyRow),
    total: Number(payload.total || payload.totalElements || 0)
  };
}

async function loadApplyData() {
  if (!currentRow.value) return;
  try {
    const response = await loadRegisteredApplies(String(currentRow.value.id), {
      pageNumber: applyQuery.pageNumber,
      pageSize: applyQuery.pageSize,
      keyword: applyQuery.keyword.trim()
    });
    applyRows.value = response.records;
    applyQuery.total = response.total || response.records.length;
  } catch (_error) {
    applyRows.value = [];
    applyQuery.total = 0;
    message("秒杀报名商品加载失败", { type: "error" });
  }
}

function searchApplyData() {
  applyQuery.pageNumber = 1;
  loadApplyData();
}

function handleApplyPageChange(page: number) {
  applyQuery.pageNumber = page;
  loadApplyData();
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
  applyQuery.keyword = "";
  applyQuery.pageNumber = 1;
  applyQuery.total = 0;
  applyVisible.value = true;
  await loadApplyData();
}

async function openAddGoods(row: Record<string, any>) {
  currentRow.value = row;
  resetSkuSelection();
  addGoodsForm.timeLine = currentTimeLineOptions.value[0];
  skuRows.value = [];
  skuQuery.keyword = "";
  skuQuery.pageNumber = 1;
  skuQuery.total = 0;
  addGoodsVisible.value = true;
  try {
    await loadSkuData();
  } catch (_error) {
    message("可添加商品加载失败", { type: "error" });
  }
}

async function submitApply() {
  if (!currentRow.value) return;
  if (!selectedApplyRows.value.length) {
    message("请选择需要添加的秒杀商品", { type: "warning" });
    return;
  }
  if (addGoodsForm.timeLine === undefined || addGoodsForm.timeLine === null) {
    message("请选择秒杀场次", { type: "warning" });
    return;
  }
  applySaving.value = true;
  try {
    const invalidRow = selectedApplyRows.value.find(item => {
      const quantity = toNumber(item.applyQuantity);
      const stock = Number(item.stock || 0);
      return quantity <= 0 || quantity > stock;
    });
    if (invalidRow) {
      message(`「${invalidRow.goodsName}」活动库存需大于 0，且不能超过商品库存`, {
        type: "warning"
      });
      return;
    }
    await addSeckillApply(
      String(currentRow.value.id),
      selectedApplyRows.value.map(item => ({
        skuId: item.skuId,
        timeLine: addGoodsForm.timeLine,
        price: toNumber(item.seckillPrice),
        quantity: toNumber(item.applyQuantity)
      }))
    );
    message("秒杀商品添加成功", { type: "success" });
    resetSkuSelection();
    addGoodsVisible.value = false;
    await loadData();
  } catch (_error) {
    message("秒杀商品添加失败，请检查场次、价格、库存或活动冲突", { type: "error" });
  } finally {
    applySaving.value = false;
  }
}

async function handleSaveApply(row: Record<string, any>) {
  if (!currentRow.value) return;
  if (toNumber(row.quantity) <= 0) {
    message("活动库存必须大于 0", { type: "warning" });
    return;
  }
  applyRowSavingId.value = String(row.id);
  try {
    await updateSeckillApply(String(currentRow.value.id), String(row.id), {
      skuId: row.skuId,
      timeLine: row.timeLine,
      price: toNumber(row.price),
      quantity: toNumber(row.quantity)
    });
    message("报名商品已更新", { type: "success" });
    await loadApplyData();
    await loadData();
  } catch (_error) {
    message("报名商品更新失败，请检查秒杀价、活动库存或商品状态", { type: "error" });
  } finally {
    applyRowSavingId.value = "";
  }
}

async function handleDeleteApply(row: Record<string, any>) {
  if (!currentRow.value) return;
  try {
    await deleteSeckillApply(String(currentRow.value.id), String(row.id));
    message("报名商品移除成功", { type: "success" });
    await loadApplyData();
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
      <el-button link type="primary" @click="openAddGoods(row)">添加商品</el-button>
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

  <el-dialog v-model="addGoodsVisible" title="添加秒杀商品" width="1120px">
    <div class="section-header apply-toolbar">
      <span>{{ currentRow?.promotionName || "当前活动" }}</span>
      <div class="sku-search">
        <el-input
          v-model="skuQuery.keyword"
          clearable
          placeholder="搜索商品名 / 规格 / 货号 / 条码"
          @keyup.enter="searchSkuData"
        />
        <el-button type="primary" @click="searchSkuData">查询</el-button>
      </div>
    </div>
    <pure-table
      :key="skuTableKey"
      row-key="id"
      :data="skuRows"
      :columns="selectableSkuColumns"
      :loading="skuLoading"
      @selection-change="handleSkuSelectionChange"
    >
      <template #seckillPrice="{ row }">
        <el-input-number
          v-model="row.seckillPrice"
          :min="0"
          :max="row.price"
          :precision="2"
          :step="1"
          controls-position="right"
          class="table-number-input"
        />
      </template>
      <template #applyQuantity="{ row }">
        <el-input-number
          v-model="row.applyQuantity"
          :min="1"
          :max="row.stock"
          :precision="0"
          :step="1"
          controls-position="right"
          class="table-number-input"
        />
      </template>
    </pure-table>
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="skuQuery.pageNumber"
        :page-size="skuQuery.pageSize"
        :total="skuQuery.total"
        layout="prev, pager, next, total"
        background
        @current-change="handleSkuPageChange"
      />
    </div>
    <section class="goods-card apply-form">
      <div class="section-header selected-header">
        <strong>批量设置</strong>
        <span>共 {{ selectedApplyRows.length }} 个</span>
      </div>
      <el-form label-width="90px">
        <el-form-item label="秒杀场次">
          <el-select v-model="addGoodsForm.timeLine" style="width: 220px" @change="handleTimeLineChange">
            <el-option
              v-for="hour in currentTimeLineOptions"
              :key="hour"
              :label="`${hour}:00`"
              :value="hour"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="form-actions">
        <el-button @click="addGoodsVisible = false">取消</el-button>
        <el-button type="primary" :loading="applySaving" @click="submitApply">
          保存商品
        </el-button>
      </div>
    </section>
  </el-dialog>

  <el-dialog v-model="applyVisible" title="秒杀报名商品" width="1100px">
    <div class="section-header apply-toolbar">
      <span>{{ currentRow?.promotionName || "当前活动" }}</span>
      <div class="sku-search">
        <el-input
          v-model="applyQuery.keyword"
          clearable
          placeholder="搜索报名商品名称"
          @keyup.enter="searchApplyData"
        />
        <el-button type="primary" @click="searchApplyData">查询</el-button>
      </div>
    </div>
    <pure-table row-key="id" :data="applyRows" :columns="applyColumns">
      <template #price="{ row }">
        <el-input-number
          v-model="row.price"
          :min="0"
          :max="row.originalPrice || undefined"
          :precision="2"
          :step="1"
          controls-position="right"
          class="table-number-input"
        />
      </template>
      <template #quantity="{ row }">
        <el-input-number
          v-model="row.quantity"
          :min="1"
          :precision="0"
          :step="1"
          controls-position="right"
          class="table-number-input"
        />
      </template>
      <template #operation="{ row }">
        <el-button
          link
          type="primary"
          :loading="applyRowSavingId === String(row.id)"
          @click="handleSaveApply(row)"
        >
          保存
        </el-button>
        <el-button link type="danger" @click="handleDeleteApply(row)">移除</el-button>
      </template>
    </pure-table>
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="applyQuery.pageNumber"
        :page-size="applyQuery.pageSize"
        :total="applyQuery.total"
        layout="prev, pager, next, total"
        background
        @current-change="handleApplyPageChange"
      />
    </div>
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

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.apply-toolbar {
  margin-bottom: 12px;
}

.goods-card {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.apply-form {
  background: var(--el-fill-color-lighter);
}

.selected-header {
  margin-bottom: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sku-search {
  display: flex;
  width: min(520px, 100%);
  gap: 8px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin: 12px 0 16px;
}

.table-number-input {
  width: 118px;
}

@media (width <= 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    align-items: stretch;
    flex-direction: column;
  }

  .sku-search {
    width: 100%;
  }
}
</style>
