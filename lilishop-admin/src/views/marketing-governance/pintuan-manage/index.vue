<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  addPintuanGoods,
  createPintuan,
  deletePintuanGoods,
  deletePintuan,
  getPintuanAvailableSkuPage,
  getPintuanDetail,
  getPintuanGoodsPage,
  getPintuanMembers,
  getPintuanPage,
  updatePintuan,
  updatePintuanGoods,
  updatePintuanStatus
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
  toNumber,
  toTimeValue
} from "../shared/promotion-helpers";

defineOptions({
  name: "PintuanManage"
});

type AnyRecord = Record<string, any>;

const rows = ref<AnyRecord[]>([]);
const selectedRows = ref<AnyRecord[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const statusVisible = ref(false);
const skuPickerVisible = ref(false);
const goodsVisible = ref(false);
const saving = ref(false);
const goodsSaving = ref(false);
const skuLoading = ref(false);
const goodsRowSavingId = ref("");
const editingId = ref("");
const detail = ref<AnyRecord>({});
const currentRow = ref<AnyRecord | null>(null);
const goodsRows = ref<AnyRecord[]>([]);
const memberRows = ref<AnyRecord[]>([]);
const statusTargetIds = ref<string[]>([]);
const statusTargetLabel = ref("");
const statusSubmitting = ref(false);
const skuRows = ref<ReturnType<typeof normalizePromotionSku>[]>([]);
const selectedSkuRows = ref<ReturnType<typeof normalizePromotionSku>[]>([]);
const skuTableRef = ref();
const existingSkuIds = ref<Set<string>>(new Set());

const query = reactive({
  keyword: "",
  status: ""
});

const extraFilters = reactive({
  goodsKeyword: ""
});

const statusForm = reactive({
  startTime: "",
  endTime: ""
});

const skuQuery = reactive({
  keyword: "",
  pageNumber: 1,
  pageSize: 10,
  total: 0
});

const goodsQuery = reactive({
  keyword: "",
  pageNumber: 1,
  pageSize: 10,
  total: 0
});

const pintuanForm = reactive(createPintuanForm());

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "商品名称", prop: "goodsName", minWidth: 220, showOverflowTooltip: true },
  { label: "成团人数", prop: "requiredNum", minWidth: 110 },
  { label: "限购数量", prop: "limitNum", minWidth: 110 },
  { label: "拼团价", prop: "pintuanPriceText", minWidth: 120 },
  { label: "活动状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "活动时间", prop: "activityTime", minWidth: 240 },
  { label: "操作", prop: "operation", fixed: "right", width: 380, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item =>
    extraFilters.goodsKeyword
      ? String(item.goodsName || "").includes(extraFilters.goodsKeyword)
      : true
  )
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
    label: "进行中活动",
    value: filteredRows.value.filter(item => item.promotionStatus === "START").length,
    accent: "green" as const,
    hint: "当前有效拼团"
  },
  {
    label: "商品覆盖",
    value: [...new Set(filteredRows.value.map(item => item.goodsName))].filter(Boolean).length,
    accent: "blue" as const,
    hint: "参与拼团商品数"
  },
  {
    label: "治理动作",
    value: "增改删/状态/导出",
    accent: "purple" as const,
    hint: "正式业务维护页"
  }
]);

const detailItems = computed(() => [
  { label: "活动名称", value: detail.value.promotionName || "-" },
  { label: "成团人数", value: detail.value.requiredNum || "-" },
  { label: "限购数量", value: detail.value.limitNum || "-" },
  { label: "虚拟成团", value: detail.value.fictitious ? "开启" : "关闭" },
  { label: "活动状态", value: detail.value.promotionStatusLabel || "-" },
  { label: "活动时间", value: detail.value.activityTime || "-" },
  { label: "拼团规则", value: detail.value.pintuanRule || "-", span: 2 }
]);

function createPintuanForm() {
  return {
    promotionName: "",
    startTime: "",
    endTime: "",
    requiredNum: 2,
    limitNum: 1,
    fictitious: false,
    pintuanRule: ""
  };
}

function resetPintuanForm() {
  Object.assign(pintuanForm, createPintuanForm());
}

function normalizePromotionSku(item: AnyRecord) {
  const skuId = String(item.id || item.skuId || "").trim();
  const goodsName = item.goodsName || item.name || "未命名商品";
  const goodsId = String(item.goodsId || "").trim();
  const simpleSpecs = String(item.simpleSpecs || "").trim();
  const sn = String(item.sn || "").trim();
  const barcode = String(item.barcode || "").trim();
  const storeName = String(item.storeName || "").trim();
  const specText = simpleSpecs ? ` / ${simpleSpecs}` : "";
  const codeText = [sn, barcode].filter(Boolean).join(" / ");
  return {
    label: codeText ? `${goodsName}${specText} (${codeText})` : `${goodsName}${specText}`,
    skuId,
    goodsId,
    goodsName,
    simpleSpecs,
    sn,
    barcode,
    storeName,
    thumbnail: item.thumbnail || "",
    originalPrice: toNumber(item.price || item.originalPrice),
    pintuanPrice: toNumber(item.pintuanPrice || item.promotionPrice || item.price || item.originalPrice),
    quantity: toNumber(item.quantity || item.stock),
    limitNum: toNumber(item.limitNum || currentRow.value?.limitNum || 0)
  };
}

async function loadPromotionSkus() {
  if (!currentRow.value) return;
  skuLoading.value = true;
  try {
    const response = await getPintuanAvailableSkuPage(String(currentRow.value.id), {
      pageNumber: skuQuery.pageNumber,
      pageSize: skuQuery.pageSize,
      skuKeyword: skuQuery.keyword.trim() || undefined
    });
    const payload = extractApiPayload<AnyRecord>(response) || {};
    const records = extractApiRecords(response);
    skuRows.value = records
      .map(normalizePromotionSku)
      .filter(item => item.skuId && !existingSkuIds.value.has(item.skuId));
    skuQuery.total = Number(payload.total ?? records.length ?? 0);
  } catch (_error) {
    skuRows.value = [];
    skuQuery.total = 0;
    message("商品搜索失败", { type: "error" });
  } finally {
    skuLoading.value = false;
  }
}

async function openSkuPicker(row: AnyRecord) {
  currentRow.value = row;
  clearSkuSelection();
  skuRows.value = [];
  skuQuery.keyword = "";
  skuQuery.pageNumber = 1;
  skuQuery.total = 0;
  skuPickerVisible.value = true;
  await nextTick();
  clearSkuSelection();
  await loadExistingSkuIds();
  await loadPromotionSkus();
  await nextTick();
  clearSkuSelection();
}

async function loadExistingSkuIds() {
  if (!currentRow.value) {
    existingSkuIds.value = new Set();
    return;
  }
  try {
    const response = await getPintuanGoodsPage(String(currentRow.value.id), {
      pageNumber: 1,
      pageSize: 10000
    });
    existingSkuIds.value = new Set(
      extractApiRecords(response)
        .map(item => String(item.skuId || "").trim())
        .filter(Boolean)
    );
  } catch (_error) {
    existingSkuIds.value = new Set();
  }
}

function closeSkuPicker() {
  skuPickerVisible.value = false;
  clearSkuSelection();
}

function clearSkuSelection() {
  selectedSkuRows.value = [];
  skuTableRef.value?.clearSelection?.();
}

function handleSkuSearch() {
  skuQuery.pageNumber = 1;
  clearSkuSelection();
  loadPromotionSkus();
}

function handleSkuReset() {
  skuQuery.keyword = "";
  skuQuery.pageNumber = 1;
  clearSkuSelection();
  loadPromotionSkus();
}

function handleSkuPageSizeChange(size: number) {
  skuQuery.pageSize = size;
  skuQuery.pageNumber = 1;
  clearSkuSelection();
  loadPromotionSkus();
}

function handleSkuPageChange(page: number) {
  skuQuery.pageNumber = page;
  clearSkuSelection();
  loadPromotionSkus();
}

function handleSkuSelectionChange(rows: AnyRecord[]) {
  selectedSkuRows.value = rows.map(normalizePromotionSku).filter(item => item.skuId);
}

function firstPromotionGoods(item: AnyRecord) {
  return Array.isArray(item.promotionGoodsList) ? item.promotionGoodsList[0] || {} : {};
}

function normalizePintuanRecord(item: AnyRecord) {
  const goods = firstPromotionGoods(item);
  const promotionStatus = String(item.promotionStatus || item.status || "-")
    .trim()
    .toUpperCase();
  const pintuanPrice = Number(
    item.pintuanPrice || item.promotionPrice || goods.price || item.salesPrice || 0
  );
  return {
    ...normalizePromotionDetail(item),
    id: item.id || item.promotionId,
    promotionName: item.promotionName || item.title || item.promotionTitle || "-",
    goodsName: item.goodsName || goods.goodsName || item.skuName || item.goodsTitle || "-",
    requiredNum: item.requiredNum || item.needMemberNum || "-",
    limitNum: item.limitNum ?? "-",
    pintuanPriceText: pintuanPrice ? formatMoney(pintuanPrice) : "-",
    promotionStatus,
    promotionStatusLabel: getPromotionStatusLabel(promotionStatus),
    activityTime:
      item.activityTime ||
      `${formatAdminDateTime(item.startTime)} 至 ${formatAdminDateTime(item.endTime)}`
  };
}

function normalizeMember(item: AnyRecord, index: number) {
  const memberStatus = String(
    item.memberStatus || item.status || item.joinStatus || item.orderStatus || ""
  )
    .trim()
    .toUpperCase();
  return {
    id: item.id || item.memberId || item.sn || `member-${index}`,
    memberName: item.memberName || item.nickname || item.username || "-",
    mobile: item.mobile || item.memberMobile || "-",
    joinStatus:
      memberStatus === "LEADER"
        ? "团长"
        : memberStatus === "SUCCESS"
          ? "已成团"
          : memberStatus === "WAIT"
            ? "待成团"
            : memberStatus || "-",
    joinTime: formatAdminDateTime(
      item.createTime || item.joinTime || item.updateTime || ""
    )
  };
}

function normalizeGoods(item: AnyRecord, index: number) {
  const originalPriceValue = toNumber(item.originalPrice || item.goodsPrice || item.skuPrice);
  const pintuanPriceValue = toNumber(item.price || item.promotionPrice || item.pintuanPrice);
  const quantityValue = toNumber(item.quantity || item.stock);
  const limitNumValue = toNumber(item.limitNum);
  return {
    id: item.id || item.skuId || `goods-${index}`,
    skuId: String(item.skuId || "").trim(),
    goodsName: item.goodsName || item.skuName || "-",
    originalPrice: formatMoney(originalPriceValue),
    originalPriceValue,
    pintuanPrice: pintuanPriceValue ? formatMoney(pintuanPriceValue) : "-",
    pintuanPriceValue,
    stock: quantityValue || "-",
    quantityValue,
    limitNum: limitNumValue || "-",
    limitNumValue
  };
}

function uniqueGoodsBySku(list: AnyRecord[]) {
  const goodsMap = new Map<string, AnyRecord>();
  list.forEach((item, index) => {
    const normalized = normalizeGoods(item, index);
    const uniqueKey = normalized.skuId || normalized.id;
    if (!goodsMap.has(uniqueKey)) {
      goodsMap.set(uniqueKey, normalized);
    }
  });
  return Array.from(goodsMap.values());
}

function fillPintuanForm(source: AnyRecord) {
  Object.assign(pintuanForm, {
    promotionName: source.promotionName || "",
    startTime: formatDateTimeForField(source.startTime),
    endTime: formatDateTimeForField(source.endTime),
    requiredNum: Number(source.requiredNum || 2),
    limitNum: Number(source.limitNum || 1),
    fictitious: Boolean(source.fictitious),
    pintuanRule: source.pintuanRule || ""
  });
}

function buildPintuanPayload() {
  return {
    promotionName: pintuanForm.promotionName,
    startTime: pintuanForm.startTime,
    endTime: pintuanForm.endTime,
    requiredNum: toNumber(pintuanForm.requiredNum),
    limitNum: toNumber(pintuanForm.limitNum),
    fictitious: Boolean(pintuanForm.fictitious),
    pintuanRule: pintuanForm.pintuanRule,
    scopeType: "PORTION_GOODS"
  };
}

async function loadData() {
  try {
    const params: AnyRecord = {
      pageNumber: 1,
      pageSize: 200
    };
    if (query.keyword) params.promotionName = query.keyword;
    if (query.status) params.promotionStatus = query.status;
    const res = await getPintuanPage(params);
    rows.value = extractApiRecords(res).map(normalizePintuanRecord);
  } catch (_error) {
    rows.value = [];
    message("拼团活动加载失败，请稍后重试", { type: "error" });
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
  extraFilters.goodsKeyword = "";
  loadData();
}

function handleSelectionChange(list: AnyRecord[]) {
  selectedRows.value = list;
}

function openCreate() {
  editingId.value = "";
  resetPintuanForm();
  dialogVisible.value = true;
}

async function openEdit(row: AnyRecord) {
  try {
    const response = await getPintuanDetail(String(row.id));
    const target = extractApiPayload<AnyRecord>(response) || row;
    editingId.value = String(row.id);
    fillPintuanForm(target);
    dialogVisible.value = true;
  } catch (_error) {
    message("拼团活动详情加载失败", { type: "error" });
  }
}

async function openDetail(row: AnyRecord) {
  try {
    const [detailRes, memberRes, goodsRes] = await Promise.all([
      getPintuanDetail(String(row.id)),
      getPintuanMembers(String(row.id)),
      getPintuanGoodsPage(String(row.id), { pageNumber: 1, pageSize: 50 })
    ]);
    detail.value = normalizePintuanRecord(extractApiPayload<AnyRecord>(detailRes) || row);
    memberRows.value = extractApiRecords(memberRes).map(normalizeMember);
    goodsRows.value = uniqueGoodsBySku(extractApiRecords(goodsRes));
  } catch (_error) {
    detail.value = normalizePintuanRecord(row);
    memberRows.value = [];
    goodsRows.value = [];
    message("拼团活动详情加载失败，已展示基础活动信息", {
      type: "warning"
    });
  }
  detailVisible.value = true;
}

async function submitPayload() {
  if (!pintuanForm.promotionName.trim()) {
    message("请填写活动名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    if (editingId.value) {
      await updatePintuan(editingId.value, buildPintuanPayload());
      message("拼团活动更新成功", { type: "success" });
    } else {
      await createPintuan(buildPintuanPayload());
      message("拼团活动创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("拼团活动保存失败，请检查时间、商品和活动价", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: AnyRecord) {
  await ElMessageBox.confirm(`确认删除拼团活动「${row.promotionName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deletePintuan([String(row.id)]);
    message("拼团活动删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("拼团活动删除失败，可能存在进行中拼团或有效订单", { type: "error" });
  }
}

async function openGoods(row: AnyRecord) {
  currentRow.value = row;
  goodsVisible.value = true;
  goodsQuery.keyword = "";
  goodsQuery.pageNumber = 1;
  goodsQuery.total = 0;
  await loadGoodsData();
}

async function loadGoodsData() {
  if (!currentRow.value) return;
  try {
    const response = await getPintuanGoodsPage(String(currentRow.value.id), {
      pageNumber: goodsQuery.pageNumber,
      pageSize: goodsQuery.pageSize,
      goodsName: goodsQuery.keyword.trim() || undefined
    });
    const payload = extractApiPayload<AnyRecord>(response) || {};
    const records = extractApiRecords(response);
    goodsRows.value = uniqueGoodsBySku(records);
    goodsQuery.total = Number(payload.total ?? goodsRows.value.length ?? 0);
  } catch (_error) {
    goodsRows.value = [];
    goodsQuery.total = 0;
    message("拼团商品加载失败", { type: "error" });
  }
}

function searchGoodsData() {
  goodsQuery.pageNumber = 1;
  loadGoodsData();
}

function handleGoodsPageChange(page: number) {
  goodsQuery.pageNumber = page;
  loadGoodsData();
}

async function submitGoods() {
  if (!currentRow.value) return;
  const selectedSkus = Array.from(
    new Map(selectedSkuRows.value.map(item => [item.skuId, item])).values()
  );
  if (!selectedSkus.length) {
    message("请选择需要添加的拼团商品", { type: "warning" });
    return;
  }
  goodsSaving.value = true;
  try {
    await addPintuanGoods(
      String(currentRow.value.id),
      selectedSkus.map(item => ({
        skuId: item.skuId,
        price: toNumber(item.pintuanPrice),
        quantity: item.quantity,
        limitNum: item.limitNum || toNumber(currentRow.value?.limitNum || 0)
      }))
    );
    message("拼团商品添加成功", { type: "success" });
    selectedSkus.forEach(item => existingSkuIds.value.add(item.skuId));
    closeSkuPicker();
    await loadData();
  } catch (_error) {
    message("拼团商品添加失败，可能存在秒杀/拼团时间冲突", { type: "error" });
  } finally {
    goodsSaving.value = false;
  }
}

async function handleSaveGoods(row: AnyRecord) {
  if (!currentRow.value) return;
  goodsRowSavingId.value = String(row.id);
  try {
    await updatePintuanGoods(String(currentRow.value.id), String(row.id), {
      skuId: row.skuId,
      price: toNumber(row.pintuanPriceValue),
      quantity: toNumber(row.quantityValue),
      limitNum: toNumber(row.limitNumValue)
    });
    message("拼团商品已更新", { type: "success" });
    await loadGoodsData();
    await loadData();
  } catch (_error) {
    message("拼团商品更新失败，请检查拼团价、库存和活动冲突", { type: "error" });
  } finally {
    goodsRowSavingId.value = "";
  }
}

async function handleDeleteGoods(row: AnyRecord) {
  if (!currentRow.value) return;
  try {
    await deletePintuanGoods(String(currentRow.value.id), String(row.id));
    message("拼团商品移除成功", { type: "success" });
    await loadGoodsData();
    await loadData();
  } catch (_error) {
    message("拼团商品移除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的拼团活动", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个拼团活动吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deletePintuan(selectedIds.value);
    selectedRows.value = [];
    message("拼团活动批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("拼团活动批量删除失败，可能存在进行中拼团或有效订单", { type: "error" });
  }
}

function isDisplaying(row: AnyRecord) {
  return String(row.promotionStatus || "").toUpperCase() === "START";
}

function openStatusDialog(row: AnyRecord) {
  const id = String(row.id || "").trim();
  if (!id) {
    message("未找到需要开启展示的拼团活动", { type: "warning" });
    return;
  }
  statusTargetIds.value = [id];
  statusTargetLabel.value = row.promotionName || id;
  statusForm.startTime = formatDateTimeForField(row.startTime);
  statusForm.endTime = formatDateTimeForField(row.endTime);
  statusVisible.value = true;
}

async function closeDisplay(row?: AnyRecord) {
  const ids = row ? [String(row.id || "").trim()].filter(Boolean) : selectedIds.value;
  if (!ids.length) {
    message("请先勾选需要关闭展示的拼团活动", { type: "warning" });
    return;
  }
  const label = row
    ? row.promotionName || String(row.id || "")
    : `已勾选的 ${ids.length} 个拼团活动`;
  await ElMessageBox.confirm(
    `确认关闭「${label}」的移动端拼团展示吗？关闭后移动端不再展示这些拼团入口。`,
    "关闭展示确认",
    { type: "warning" }
  );
  try {
    await updatePintuanStatus(ids.join(","), { open: false });
    message("拼团活动展示已关闭", { type: "success" });
    selectedRows.value = [];
    await loadData();
  } catch (_error) {
    message("拼团活动关闭展示失败", { type: "error" });
  }
}

async function submitStatus() {
  if (!statusTargetIds.value.length) return;
  if (!statusForm.startTime || !statusForm.endTime) {
    message("请选择移动端展示开始时间和结束时间", { type: "warning" });
    return;
  }
  if (statusTargetIds.value.length > 1) {
    message("移动端展示拼团一次只能开启一个活动", { type: "warning" });
    return;
  }
  statusSubmitting.value = true;
  try {
    await updatePintuanStatus(statusTargetIds.value[0], {
      open: true,
      startTime: toTimeValue(statusForm.startTime),
      endTime: toTimeValue(statusForm.endTime)
    });
    message("拼团活动已开启移动端展示", { type: "success" });
    statusVisible.value = false;
    await loadData();
  } catch (_error) {
    message("拼团活动开启展示失败", { type: "error" });
  } finally {
    statusSubmitting.value = false;
  }
}

function exportPintuan() {
  if (!filteredRows.value.length) {
    message("暂无可导出的拼团活动数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    活动名称: item.promotionName,
    商品名称: item.goodsName,
    成团人数: item.requiredNum,
    限购数量: item.limitNum,
    拼团价: item.pintuanPriceText,
    活动状态: item.promotionStatusLabel,
    活动时间: item.activityTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "拼团活动管理");
  writeFile(workbook, "拼团活动管理.xlsx");
  message("拼团活动数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="拼团活动管理"
    description="承接平台拼团活动列表、新增、编辑、删除、状态治理、商品明细、成员记录和导出。"
    api-path="/manager/promotion/pintuan"
    :columns="columns"
    :data="filteredRows"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' },
      { label: '已关闭', value: 'CLOSE' }
    ]"
    keyword-label="活动"
    keyword-placeholder="请输入拼团活动名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button type="warning" plain @click="closeDisplay()">
        批量关闭展示
      </el-button>
      <el-button @click="exportPintuan">导出</el-button>
      <el-button type="primary" @click="openCreate">新增拼团活动</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="商品名称">
        <el-input
          v-model="extraFilters.goodsKeyword"
          placeholder="请输入商品名称"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button
        v-if="isDisplaying(row)"
        link
        type="warning"
        @click="closeDisplay(row)"
      >
        关闭展示
      </el-button>
      <el-button v-else link type="warning" @click="openStatusDialog(row)">
        开启展示
      </el-button>
      <el-button link type="primary" @click="openSkuPicker(row)">添加商品</el-button>
      <el-button link type="primary" @click="openGoods(row)">活动商品</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑拼团活动' : '新增拼团活动'"
    width="980px"
  >
    <el-form label-width="110px">
      <div class="form-grid">
        <el-form-item label="活动名称">
          <el-input v-model="pintuanForm.promotionName" />
        </el-form-item>
        <el-form-item label="虚拟成团">
          <el-switch v-model="pintuanForm.fictitious" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="pintuanForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="pintuanForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="成团人数">
          <el-input-number
            v-model="pintuanForm.requiredNum"
            :min="2"
            :max="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="限购数量">
          <el-input-number
            v-model="pintuanForm.limitNum"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <el-form-item label="拼团规则">
        <el-input
          v-model="pintuanForm.pintuanRule"
          type="textarea"
          :rows="3"
          placeholder="请输入拼团规则说明"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="skuPickerVisible"
    title="选择拼团商品"
    width="1080px"
    append-to-body
    @closed="clearSkuSelection"
  >
    <el-form class="sku-picker-filter" inline @submit.prevent>
      <el-form-item label="商品">
        <el-input
          v-model="skuQuery.keyword"
          clearable
          placeholder="商品名 / 规格 / 货号 / 条码"
          @keyup.enter="handleSkuSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSkuSearch">查询</el-button>
        <el-button @click="handleSkuReset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table
      ref="skuTableRef"
      v-loading="skuLoading"
      :data="skuRows"
      border
      height="430"
      row-key="skuId"
      @selection-change="handleSkuSelectionChange"
    >
      <el-table-column type="selection" width="54" />
      <el-table-column label="商品" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="sku-table-name">
            <span>{{ row.goodsName }}</span>
            <small v-if="row.simpleSpecs">{{ row.simpleSpecs }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="SKU ID" prop="skuId" min-width="170" show-overflow-tooltip />
      <el-table-column label="货号" prop="sn" min-width="130" show-overflow-tooltip />
      <el-table-column label="条码" prop="barcode" min-width="150" show-overflow-tooltip />
      <el-table-column label="店铺" prop="storeName" min-width="150" show-overflow-tooltip />
      <el-table-column label="价格" min-width="100">
        <template #default="{ row }">¥ {{ row.originalPrice.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="拼团价" min-width="150">
        <template #default="{ row }">
          <el-input-number
            v-model="row.pintuanPrice"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            class="table-number-input"
          />
        </template>
      </el-table-column>
      <el-table-column label="库存" prop="quantity" min-width="90" />
      <el-table-column label="限购" min-width="130">
        <template #default="{ row }">
          <el-input-number
            v-model="row.limitNum"
            :min="0"
            :precision="0"
            :step="1"
            controls-position="right"
            class="table-number-input"
          />
        </template>
      </el-table-column>
    </el-table>
    <div class="sku-picker-pagination">
      <el-pagination
        v-model:current-page="skuQuery.pageNumber"
        v-model:page-size="skuQuery.pageSize"
        :total="skuQuery.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSkuPageSizeChange"
        @current-change="handleSkuPageChange"
      />
    </div>
    <template #footer>
      <el-button @click="closeSkuPicker">取消</el-button>
      <el-button type="primary" :loading="goodsSaving" @click="submitGoods">
        保存商品
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="goodsVisible" title="拼团活动商品" width="1080px">
    <div class="section-header sku-picker-filter">
      <span>{{ currentRow?.promotionName || "当前活动" }}</span>
      <div class="sku-search">
        <el-input
          v-model="goodsQuery.keyword"
          clearable
          placeholder="搜索商品名称"
          @keyup.enter="searchGoodsData"
        />
        <el-button type="primary" @click="searchGoodsData">查询</el-button>
      </div>
    </div>
    <el-table :data="goodsRows" border height="430" row-key="id">
      <el-table-column label="商品名称" prop="goodsName" min-width="220" show-overflow-tooltip />
      <el-table-column label="原价" prop="originalPrice" min-width="120" />
      <el-table-column label="拼团价" min-width="150">
        <template #default="{ row }">
          <el-input-number
            v-model="row.pintuanPriceValue"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            class="table-number-input"
          />
        </template>
      </el-table-column>
      <el-table-column label="库存" min-width="130">
        <template #default="{ row }">
          <el-input-number
            v-model="row.quantityValue"
            :min="0"
            :precision="0"
            :step="1"
            controls-position="right"
            class="table-number-input"
          />
        </template>
      </el-table-column>
      <el-table-column label="限购" min-width="130">
        <template #default="{ row }">
          <el-input-number
            v-model="row.limitNumValue"
            :min="0"
            :precision="0"
            :step="1"
            controls-position="right"
            class="table-number-input"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="132">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :loading="goodsRowSavingId === String(row.id)"
            @click="handleSaveGoods(row)"
          >
            保存
          </el-button>
          <el-button link type="danger" @click="handleDeleteGoods(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="sku-picker-pagination">
      <el-pagination
        v-model:current-page="goodsQuery.pageNumber"
        :page-size="goodsQuery.pageSize"
        :total="goodsQuery.total"
        layout="prev, pager, next, total"
        background
        @current-change="handleGoodsPageChange"
      />
    </div>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="拼团活动详情" size="760px">
    <el-descriptions :column="2" border class="mb-4">
      <el-descriptions-item
        v-for="item in detailItems"
        :key="item.label"
        :label="item.label"
        :span="item.span || 1"
      >
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>

    <h3 class="drawer-title">待成团成员</h3>
    <el-table :data="memberRows" border class="mb-4">
      <el-table-column label="成员名称" prop="memberName" min-width="180" />
      <el-table-column label="手机号" prop="mobile" min-width="160" />
      <el-table-column label="参与状态" prop="joinStatus" min-width="120" />
      <el-table-column label="参与时间" prop="joinTime" min-width="180" />
    </el-table>

    <h3 class="drawer-title">关联商品</h3>
    <el-table :data="goodsRows" border>
      <el-table-column label="商品名称" prop="goodsName" min-width="220" />
      <el-table-column label="原价" prop="originalPrice" min-width="120" />
      <el-table-column label="拼团价" prop="pintuanPrice" min-width="120" />
      <el-table-column label="库存" prop="stock" min-width="120" />
      <el-table-column label="限购" prop="limitNum" min-width="120" />
    </el-table>
  </el-drawer>

  <el-dialog v-model="statusVisible" title="开启移动端拼团展示" width="520px">
    <el-alert
      :title="`当前将开启：${statusTargetLabel}`"
      description="移动端拼团入口一次只展示一个活动，开启后后端会自动关闭其他正在展示的拼团活动。"
      type="warning"
      :closable="false"
      show-icon
      class="status-alert"
    />
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
      <el-button type="primary" :loading="statusSubmitting" @click="submitStatus">
        开启展示
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.sku-picker-filter {
  margin-bottom: 12px;
}

.sku-search {
  display: flex;
  width: min(520px, 100%);
  gap: 8px;
}

.sku-table-name {
  display: grid;
  gap: 3px;
  min-width: 0;
  line-height: 1.35;
}

.sku-table-name span,
.sku-table-name small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sku-table-name small {
  color: #909399;
}

.sku-picker-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.table-number-input {
  width: 118px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.drawer-title {
  margin: 0 0 12px;
  color: #2d2f33;
  font-size: 16px;
}

.status-alert {
  margin-bottom: 16px;
}

@media (width <= 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
