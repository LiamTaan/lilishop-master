<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  auditWholesaleGoods,
  createGoodsUnit,
  deleteWholesaleGoods,
  deleteGoodsUnits,
  getGoodsPage,
  getGoodsUnitDetail,
  getGoodsUnitPage,
  getWholesaleGoodsDetail,
  underWholesaleGoods,
  updateGoodsUnit,
  upperWholesaleGoods
} from "@/api/goods-governance";
import GoodsEditorDialog from "./GoodsEditorDialog.vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getGoodsAuditStatusLabel,
  getGoodsMarketStatusLabel,
  getGoodsTypeLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "GoodsManage"
});

const router = useRouter();
const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailData = ref<Record<string, any> | null>(null);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorData = ref<Record<string, any> | null>(null);
const currentRow = ref<Record<string, any> | null>(null);
const selectedRows = ref<Record<string, any>[]>([]);
const goodsUnitVisible = ref(false);
const goodsUnitLoading = ref(false);
const goodsUnitSaving = ref(false);
const goodsUnitFormVisible = ref(false);
const goodsUnitRows = ref<Record<string, any>[]>([]);
const goodsUnitEditingId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  goodsCode: "",
  goodsType: "",
  salesModel: "",
  specText: "",
  dateRange: [] as string[]
});
const goodsUnitForm = reactive({
  name: ""
});

const structuredGoodsParamFields = [
  { key: "productionDate", label: "生产日期" },
  { key: "shelfLife", label: "保质期" },
  { key: "origin", label: "产地" },
  { key: "goodsOrigin", label: "产地" },
  { key: "placeOfOrigin", label: "产地" },
  { key: "netContent", label: "净含量" },
  { key: "storageMethod", label: "储存方式" },
  { key: "afterSaleScope", label: "售后范围" },
  { key: "goodsLevel", label: "品级" }
] as const;

function normalizeStringList(source: unknown) {
  if (!Array.isArray(source)) {
    return [];
  }
  return source.map(item => String(item || "").trim()).filter(Boolean);
}

function normalizeCategoryNames(item: Record<string, any>) {
  if (Array.isArray(item.categoryName)) {
    return item.categoryName
      .map((name: unknown) => String(name || "").trim())
      .filter(Boolean);
  }
  if (typeof item.categoryName === "string" && item.categoryName.trim()) {
    return item.categoryName
      .split(/[,/]/)
      .map(name => name.trim())
      .filter(Boolean);
  }
  return [];
}

function parseJsonSafely(source: string) {
  try {
    return JSON.parse(source);
  } catch (_error) {
    return source;
  }
}

function normalizeGoodsParams(source: unknown): Record<string, any>[] {
  if (typeof source === "string") {
    const parsed = parseJsonSafely(source.trim());
    if (parsed !== source) {
      return normalizeGoodsParams(parsed);
    }
    return [];
  }
  if (!source || typeof source !== "object") {
    return [];
  }

  const normalized: Record<string, any>[] = [];
  const seen = new Set<string>();

  const appendParam = (item: Record<string, any>, inheritedGroupName = "") => {
    const paramName = String(item?.paramName || item?.name || "").trim();
    const paramValue = String(item?.paramValue || item?.value || "").trim();
    const groupName = String(
      item?.groupName || inheritedGroupName || ""
    ).trim();
    if (!paramName && !paramValue) {
      return;
    }
    const dedupeKey = `${groupName}::${paramName}::${paramValue}`;
    if (seen.has(dedupeKey)) {
      return;
    }
    seen.add(dedupeKey);
    normalized.push({
      paramName,
      paramValue,
      groupName
    });
  };

  const visit = (input: unknown, inheritedGroupName = "") => {
    if (Array.isArray(input)) {
      input.forEach(item => visit(item, inheritedGroupName));
      return;
    }
    if (!input || typeof input !== "object") {
      return;
    }
    const item = input as Record<string, any>;
    if (Array.isArray(item.goodsParamsItemDTOList)) {
      const groupName = String(
        item.groupName || inheritedGroupName || ""
      ).trim();
      item.goodsParamsItemDTOList.forEach((child: Record<string, any>) =>
        visit({ ...child, groupName }, groupName)
      );
      return;
    }
    appendParam(item, inheritedGroupName);
  };

  visit(source);
  return normalized;
}

function normalizeStructuredGoodsParams(item: Record<string, any>) {
  return structuredGoodsParamFields
    .map(({ key, label }) => ({
      key,
      paramName: label,
      paramValue: String(item?.[key] || "").trim()
    }))
    .filter(item => item.paramValue);
}

function mergeGoodsParams(
  ...sources: Array<Record<string, any>[] | undefined>
): Record<string, any>[] {
  const merged: Record<string, any>[] = [];
  const seen = new Set<string>();
  sources.forEach(source => {
    source?.forEach(item => {
      const paramName = String(item?.paramName || "").trim();
      const paramValue = String(item?.paramValue || "").trim();
      const groupName = String(item?.groupName || "").trim();
      if (!paramName && !paramValue) {
        return;
      }
      const dedupeKey = `${groupName}::${paramName}::${paramValue}`;
      if (seen.has(dedupeKey)) {
        return;
      }
      seen.add(dedupeKey);
      merged.push({
        ...item,
        paramName,
        paramValue,
        groupName
      });
    });
  });
  return merged;
}

function normalizeSkuList(source: unknown): Record<string, any>[] {
  if (!Array.isArray(source)) {
    return [];
  }
  return source.map((sku: Record<string, any>, index: number) => {
    const specList = Array.isArray(sku?.specList)
      ? sku.specList.filter(Boolean)
      : [];
    const specSummary =
      specList
        .filter((spec: Record<string, any>) => spec?.specName !== "images")
        .map((spec: Record<string, any>) =>
          String(spec?.specValue || "").trim()
        )
        .filter(Boolean)
        .join(" / ") ||
      String(sku?.simpleSpecs || "").trim() ||
      `规格${index + 1}`;
    const goodsGalleryList = normalizeStringList(sku?.goodsGalleryList);
    return {
      ...sku,
      displayCode: sku?.sn || sku?.id || "-",
      displayBarcode: sku?.barcode || "-",
      specList,
      specSummary,
      goodsGalleryList
    };
  });
}

function getVisibleSkuSpecs(specList: unknown): Record<string, any>[] {
  if (!Array.isArray(specList)) {
    return [];
  }
  return specList.filter(
    (item: Record<string, any>) => item?.specName && item.specName !== "images"
  );
}

function getSalesModelLabel(value: unknown) {
  const upperValue = String(value || "").toUpperCase();
  if (upperValue === "WHOLESALE" || upperValue === "WHOLE_SALE") {
    return "批发";
  }
  if (upperValue === "RETAIL") {
    return "零售";
  }
  return upperValue || "-";
}

const summaryCards = computed<
  Array<{
    label: string;
    value: string | number;
    accent: "orange" | "blue" | "green" | "purple";
    hint: string;
  }>
>(() => {
  const total = filteredData.value.length;
  const approved = filteredData.value.filter(
    item => String(item.authFlag).toUpperCase() === "PASS"
  ).length;
  const online = filteredData.value.filter(
    item => String(item.marketEnable).toUpperCase() === "UPPER"
  ).length;
  const totalStock = filteredData.value.reduce(
    (sum, item) => sum + Number(item.stock || 0),
    0
  );
  return [
    { label: "商品总数", value: total, accent: "orange", hint: "当前筛选结果" },
    {
      label: "已通过商品",
      value: approved,
      accent: "green",
      hint: "审核通过的商品"
    },
    {
      label: "已上架商品",
      value: online,
      accent: "purple",
      hint: "当前正在销售"
    },
    {
      label: "库存件数",
      value: totalStock,
      accent: "blue",
      hint: "用于运营盘货"
    },
    {
      label: "治理动作",
      value: "已接入",
      accent: "orange",
      hint: "详情/审核/上架/下架/删除"
    }
  ];
});

const selectedIds = computed(() =>
  selectedRows.value.map(item => String(item.id || "").trim()).filter(Boolean)
);

function normalizeGoodsRecord(item: Record<string, any>) {
  const isWholesaleGoods =
    String(item.salesModel || "").toUpperCase() === "WHOLESALE";
  const authFlag = item.authFlag || item.auditStatus || "TOBEAUDITED";
  const marketEnable = item.marketEnable || item.goodsStatus || "DOWN";
  const deleteFlag = Boolean(item.deleteFlag);
  const skuList: Record<string, any>[] = normalizeSkuList(item.skuList);
  const goodsGalleryList = normalizeStringList(item.goodsGalleryList);
  const categoryNameList = normalizeCategoryNames(item);
  const specText =
    String(item.simpleSpecs || "").trim() ||
    (skuList.length === 1
      ? skuList[0].specSummary
      : skuList.length > 1
        ? `共 ${skuList.length} 个规格`
        : "-");
  const skuSpecMap = new Map(
    skuList.map(sku => [String(sku.id || ""), String(sku.specSummary || "-")])
  );
  const wholesaleList: Record<string, any>[] = Array.isArray(item.wholesaleList)
    ? item.wholesaleList
        .map(
          (rule: Record<string, any>): Record<string, any> => ({
            ...rule,
            skuSpecText: skuSpecMap.get(String(rule?.skuId || "")) || "-"
          })
        )
        .sort(
          (left: Record<string, any>, right: Record<string, any>) =>
            Number(left?.num || 0) - Number(right?.num || 0)
        )
    : [];
  const validWholesalePrices = wholesaleList
    .map(rule => Number(rule?.price || 0))
    .filter(price => price > 0);
  const skuCostPrices = skuList
    .map(sku => Number(sku?.cost || 0))
    .filter(cost => cost > 0);
  const salePrice =
    isWholesaleGoods && validWholesalePrices.length
      ? Math.min(...validWholesalePrices)
      : item.salePrice || item.price || item.originalPrice || 0;
  const costPrice =
    skuCostPrices.length > 0
      ? Math.min(...skuCostPrices)
      : item.costPrice || item.cost || item.buyingPrice || 0;
  const profitAmount =
    Number(salePrice || 0) > 0 && Number(costPrice || 0) > 0
      ? Number(salePrice) - Number(costPrice)
      : null;
  const goodsParamsDTOList = mergeGoodsParams(
    normalizeGoodsParams(item.goodsParamsDTOList),
    normalizeGoodsParams(item.params),
    normalizeStructuredGoodsParams(item)
  );
  return {
    ...item,
    id: item.id || item.goodsId,
    goodsName: item.goodsName || item.name || "-",
    thumb:
      item.thumbnail ||
      item.goodsImage ||
      item.small ||
      goodsGalleryList[0] ||
      "",
    storeName: item.storeName || item.storeNickName || "-",
    goodsCode: item.sn || item.goodsSn || item.goodsNo || item.id || "-",
    goodsType: item.goodsType || item.goodsUnit || item.goodsFormat || "",
    categoryName:
      categoryNameList.join(" / ") ||
      item.categoryPathName ||
      item.goodsCategoryName ||
      "-",
    categoryNameList,
    specText,
    costPrice,
    salePrice,
    profitAmount,
    salePriceLabel: isWholesaleGoods ? "起批价" : "售价",
    profitLabel: isWholesaleGoods ? "最低阶梯毛利" : "利润",
    stock: item.quantity || item.stock || item.goodsStock || 0,
    marketEnable,
    authFlag,
    authMessage: item.authMessage || item.auditRemark || "",
    underMessage: item.underMessage || item.underReason || "",
    goodsGalleryList,
    skuList,
    goodsParamsDTOList,
    goodsParamCount: goodsParamsDTOList.length,
    wholesaleList,
    deleteFlag,
    createTime: item.createTime || item.updateTime || "-",
    canApprove: String(authFlag).toUpperCase() === "TOBEAUDITED",
    canReject: String(authFlag).toUpperCase() === "TOBEAUDITED",
    canUpper:
      String(authFlag).toUpperCase() === "PASS" &&
      String(marketEnable).toUpperCase() === "DOWN" &&
      !deleteFlag,
    canDown:
      String(authFlag).toUpperCase() === "PASS" &&
      String(marketEnable).toUpperCase() === "UPPER" &&
      !deleteFlag
  };
}

function normalizeGoodsUnitRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.goodsUnitId,
    name: item.name || "-",
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

const filteredData = computed(() => {
  return data.value.filter(item => {
    const codeMatched = extraFilters.goodsCode
      ? String(item.goodsCode).includes(extraFilters.goodsCode)
      : true;
    const typeMatched = extraFilters.goodsType
      ? String(item.goodsType).includes(extraFilters.goodsType)
      : true;
    const salesModelMatched = extraFilters.salesModel
      ? String(item.salesModel || "").toUpperCase() ===
        String(extraFilters.salesModel).toUpperCase()
      : true;
    const specMatched = extraFilters.specText
      ? String(item.specText).includes(extraFilters.specText)
      : true;
    return codeMatched && typeMatched && salesModelMatched && specMatched;
  });
});

const detailGoodsParams = computed(() => {
  const source = detailData.value;
  if (!source) {
    return [];
  }
  return mergeGoodsParams(
    normalizeGoodsParams(source.goodsParamsDTOList),
    normalizeGoodsParams(source.params),
    normalizeStructuredGoodsParams(source)
  );
});

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10
  };
  if (query.keyword) params.goodsName = query.keyword;
  if (query.status) params.goodsStatus = query.status;
  if (extraFilters.salesModel) params.salesModel = extraFilters.salesModel;
  const res = await getGoodsPage(params);
  const page = extractApiPayload(res);
  const records = extractApiRecords(page).map(normalizeGoodsRecord);
  data.value = records;
  const hydratedRecords = await Promise.all(
    records.map(async item => {
      const goodsId = String(item.id || "").trim();
      if (!goodsId) {
        return item;
      }
      try {
        const detailRes = await getWholesaleGoodsDetail(goodsId);
        return normalizeGoodsRecord({
          ...item,
          ...(extractApiPayload(detailRes) || {})
        });
      } catch (_error) {
        return item;
      }
    })
  );
  data.value = hydratedRecords;
}

async function loadGoodsUnits() {
  goodsUnitLoading.value = true;
  try {
    const res = await getGoodsUnitPage({
      pageNumber: 1,
      pageSize: 200
    });
    goodsUnitRows.value = extractApiRecords(res).map(normalizeGoodsUnitRecord);
  } catch (_error) {
    goodsUnitRows.value = [];
    message("商品单位加载失败，请稍后重试", { type: "error" });
  } finally {
    goodsUnitLoading.value = false;
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.goodsCode = "";
  extraFilters.goodsType = "";
  extraFilters.salesModel = "";
  extraFilters.specText = "";
  extraFilters.dateRange = [];
  loadData();
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getWholesaleGoodsDetail(row.id);
    detailData.value = normalizeGoodsRecord(extractApiPayload(res) || row);
  } finally {
    detailLoading.value = false;
  }
}

function openCreateEditor() {
  editorMode.value = "create";
  editorData.value = null;
  editorVisible.value = true;
}

function openParameterManage() {
  router.push("/goods-governance/parameter-manage");
}

function openCategoryManage() {
  router.push("/goods-governance/category-manage");
}

async function openEditEditor(row: Record<string, any>) {
  editorMode.value = "edit";
  try {
    const res = await getWholesaleGoodsDetail(String(row.id));
    editorData.value = normalizeGoodsRecord(extractApiPayload(res) || row);
    editorVisible.value = true;
  } catch (_error) {
    message("商品详情加载失败，暂时无法编辑", { type: "error" });
  }
}

async function handleEditorSaved() {
  await loadData();
}

async function handleAudit(
  row: Record<string, any>,
  authFlag: "PASS" | "REFUSE"
) {
  await ElMessageBox.confirm(
    `确认${authFlag === "PASS" ? "通过" : "驳回"}商品「${row.goodsName || row.id}」吗？`,
    `${authFlag === "PASS" ? "通过" : "驳回"}确认`,
    { type: authFlag === "PASS" ? "success" : "warning" }
  );
  await auditWholesaleGoods([row.id], authFlag);
  message(`商品已${authFlag === "PASS" ? "通过" : "驳回"}`, {
    type: "success"
  });
  await loadData();
}

async function handleUpper(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认上架商品「${row.goodsName || row.id}」吗？`,
    "上架确认",
    { type: "success" }
  );
  await upperWholesaleGoods([row.id]);
  message("商品已上架", { type: "success" });
  await loadData();
}

async function handleUnder(row: Record<string, any>) {
  const { value } = await ElMessageBox.prompt(
    `请输入商品「${row.goodsName || row.id}」的下架原因`,
    "下架确认",
    {
      confirmButtonText: "确认下架",
      cancelButtonText: "取消",
      inputPattern: /\S+/,
      inputErrorMessage: "下架原因不能为空"
    }
  );
  await underWholesaleGoods([row.id], value.trim());
  message("商品已下架", { type: "success" });
  await loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认删除商品「${row.goodsName || row.id}」吗？删除后会同步下架并标记删除。`,
    "删除确认",
    { type: "warning" }
  );
  await deleteWholesaleGoods([String(row.id)]);
  message("商品删除成功", { type: "success" });
  await loadData();
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的商品", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认批量删除已勾选的 ${selectedIds.value.length} 个商品吗？删除后会同步下架并标记删除。`,
    "批量删除确认",
    { type: "warning" }
  );
  await deleteWholesaleGoods(selectedIds.value);
  selectedRows.value = [];
  message("批量删除成功", { type: "success" });
  await loadData();
}

function exportGoods() {
  if (!filteredData.value.length) {
    message("暂无可导出的商品数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    商品标题: item.goodsName,
    店铺名称: item.storeName,
    商品编码: item.goodsCode,
    商品类型: getGoodsTypeLabel(item.goodsType),
    规格: item.specText,
    成本价: item.costPrice,
    售价或起批价: item.salePrice,
    利润或毛利: item.profitAmount ?? "-",
    库存: item.stock,
    销售状态: getGoodsMarketStatusLabel(item.marketEnable),
    审核状态: getGoodsAuditStatusLabel(item.authFlag),
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "商品列表");
  writeFile(workbook, "商品列表.xlsx");
  message("商品数据导出成功", { type: "success" });
}

function resetGoodsUnitForm() {
  goodsUnitEditingId.value = "";
  goodsUnitForm.name = "";
}

async function openGoodsUnitDialog() {
  goodsUnitVisible.value = true;
  await loadGoodsUnits();
}

function openGoodsUnitCreate() {
  resetGoodsUnitForm();
  goodsUnitFormVisible.value = true;
}

async function openGoodsUnitEdit(row: Record<string, any>) {
  try {
    const res = await getGoodsUnitDetail(String(row.id));
    const detail = normalizeGoodsUnitRecord(extractApiPayload(res) || row);
    goodsUnitEditingId.value = String(detail.id);
    goodsUnitForm.name = detail.name || "";
    goodsUnitFormVisible.value = true;
  } catch (_error) {
    message("商品单位详情加载失败", { type: "error" });
  }
}

async function submitGoodsUnit() {
  const name = goodsUnitForm.name.trim();
  if (!name) {
    message("请输入商品单位名称", { type: "warning" });
    return;
  }
  if (name.length > 5) {
    message("商品单位长度不能超过 5 个字符", { type: "warning" });
    return;
  }
  goodsUnitSaving.value = true;
  try {
    if (goodsUnitEditingId.value) {
      await updateGoodsUnit(goodsUnitEditingId.value, { name });
      message("商品单位更新成功", { type: "success" });
    } else {
      await createGoodsUnit({ name });
      message("商品单位新增成功", { type: "success" });
    }
    goodsUnitFormVisible.value = false;
    resetGoodsUnitForm();
    await loadGoodsUnits();
  } catch (_error) {
    message("商品单位保存失败，请确认管理端接口鉴权与字段契约", {
      type: "error"
    });
  } finally {
    goodsUnitSaving.value = false;
  }
}

async function handleGoodsUnitDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认删除商品单位「${row.name || row.id}」吗？`,
    "删除确认",
    { type: "warning" }
  );
  try {
    await deleteGoodsUnits([String(row.id)]);
    message("商品单位删除成功", { type: "success" });
    await loadGoodsUnits();
  } catch (_error) {
    message("商品单位删除失败，请确认是否已有商品正在使用该单位", {
      type: "error"
    });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="商品列表"
    description="统一维护平台商品，支持零售商品与批发商品的新增、编辑、审核、上下架、删除和导出。"
    api-path="/manager/goods/goods/list"
    :columns="columns"
    :data="filteredData"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待审核', value: 'TOBEAUDITED' },
      { label: '已通过', value: 'PASS' },
      { label: '已驳回', value: 'REFUSE' },
      { label: '已上架', value: 'UPPER' },
      { label: '已下架', value: 'DOWN' }
    ]"
    status-label="商品状态"
    keyword-label="商品名称"
    keyword-placeholder="请输入商品名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreateEditor"> 新增商品 </el-button>
      <el-button type="danger" plain @click="handleBatchDelete">
        批量删除
      </el-button>
      <el-button
        class="goods-manage__toolbar-button"
        @click="openParameterManage"
      >
        参数绑定
      </el-button>
      <el-button
        class="goods-manage__toolbar-button"
        @click="openCategoryManage"
      >
        分类管理
      </el-button>
      <el-button
        class="goods-manage__toolbar-button"
        @click="openGoodsUnitDialog"
      >
        单位管理
      </el-button>
      <el-button @click="exportGoods">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="商品编码">
        <el-input
          v-model="extraFilters.goodsCode"
          placeholder="请输入商品编码"
          clearable
        />
      </el-form-item>
      <el-form-item label="商品类型">
        <el-input
          v-model="extraFilters.goodsType"
          placeholder="请输入商品类型"
          clearable
        />
      </el-form-item>
      <el-form-item label="销售模式">
        <el-select
          v-model="extraFilters.salesModel"
          placeholder="全部模式"
          clearable
        >
          <el-option label="零售" value="RETAIL" />
          <el-option label="批发" value="WHOLESALE" />
        </el-select>
      </el-form-item>
      <el-form-item label="规格">
        <el-input
          v-model="extraFilters.specText"
          placeholder="请选择规格"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEditEditor(row)">
        编辑
      </el-button>
      <el-button
        v-if="row.canApprove"
        link
        type="success"
        @click="handleAudit(row, 'PASS')"
      >
        通过
      </el-button>
      <el-button
        v-if="row.canReject"
        link
        type="danger"
        @click="handleAudit(row, 'REFUSE')"
      >
        驳回
      </el-button>
      <el-button
        v-if="row.canUpper"
        link
        type="success"
        @click="handleUpper(row)"
      >
        上架
      </el-button>
      <el-button
        v-if="row.canDown"
        link
        type="warning"
        @click="handleUnder(row)"
      >
        下架
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="detailVisible"
    :title="`商品详情 - ${detailData?.goodsName || currentRow?.goodsName || '-'}`"
    width="820px"
  >
    <div v-loading="detailLoading" class="goods-detail">
      <div class="goods-detail__hero">
        <img
          v-if="detailData?.thumb"
          :src="detailData.thumb"
          :alt="detailData.goodsName || 'goods'"
          class="goods-detail__thumb"
        />
        <div v-else class="goods-detail__thumb goods-detail__thumb--empty">
          暂无图片
        </div>
        <div class="goods-detail__summary">
          <h3>{{ detailData?.goodsName || "-" }}</h3>
          <p>{{ detailData?.storeName || "-" }}</p>
          <div class="goods-detail__tags">
            <el-tag round effect="light">
              {{ getGoodsTypeLabel(detailData?.goodsType) }}
            </el-tag>
            <el-tag type="success" round effect="light">
              {{ getGoodsMarketStatusLabel(detailData?.marketEnable) }}
            </el-tag>
            <el-tag type="warning" round effect="light">
              {{ getGoodsAuditStatusLabel(detailData?.authFlag) }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="SPU ID">
          {{ detailData?.id || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="商品编码">
          {{ detailData?.goodsCode || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="销售模式">
          {{ getSalesModelLabel(detailData?.salesModel) }}
        </el-descriptions-item>
        <el-descriptions-item label="商品类型">
          {{ getGoodsTypeLabel(detailData?.goodsType) }}
        </el-descriptions-item>
        <el-descriptions-item label="商品单位">
          {{ detailData?.goodsUnit || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="规格">
          {{ detailData?.specText || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="分类">
          {{ detailData?.categoryName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item :label="detailData?.salePriceLabel || '售价'">
          ¥ {{ detailData?.salePrice || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="成本价">
          ¥ {{ detailData?.costPrice || "-" }}
        </el-descriptions-item>
        <el-descriptions-item :label="detailData?.profitLabel || '利润'">
          ¥ {{ detailData?.profitAmount ?? "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="库存">
          {{ detailData?.stock || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="SKU 数量">
          {{ detailData?.skuList?.length || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="详细规格">
          {{ detailGoodsParams.length }} 项
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailData?.createTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="审核说明" :span="2">
          {{ detailData?.authMessage || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="下架原因" :span="2">
          {{ detailData?.underMessage || "-" }}
        </el-descriptions-item>
      </el-descriptions>

      <section
        v-if="detailData?.goodsGalleryList?.length"
        class="goods-detail__section"
      >
        <div class="goods-detail__section-head">
          <h4>商品相册</h4>
          <span>{{ detailData.goodsGalleryList.length }} 张</span>
        </div>
        <div class="goods-gallery">
          <img
            v-for="(image, index) in detailData.goodsGalleryList"
            :key="`${image}-${index}`"
            :src="image"
            :alt="detailData.goodsName || `goods-${index}`"
            class="goods-gallery__item"
          />
        </div>
      </section>

      <section class="goods-detail__section">
        <div class="goods-detail__section-head">
          <h4>详细规格</h4>
          <span>{{ detailGoodsParams.length }} 项</span>
        </div>
        <el-descriptions
          v-if="detailGoodsParams.length"
          :column="2"
          border
          size="small"
        >
          <el-descriptions-item
            v-for="item in detailGoodsParams"
            :key="`${item.groupName || 'default'}-${item.paramName}-${item.paramValue}`"
            :label="
              item.groupName
                ? `${item.groupName} / ${item.paramName || '参数'}`
                : item.paramName || '参数'
            "
          >
            {{ item.paramValue || "-" }}
          </el-descriptions-item>
        </el-descriptions>
        <el-empty
          v-else
          description="当前商品还没有维护详细规格。可先在参数管理里维护参数模板，再由供货端商品发布页填写参数值。"
        />
      </section>

      <section v-if="detailData?.skuList?.length" class="goods-detail__section">
        <div class="goods-detail__section-head">
          <h4>SKU 规格表</h4>
          <span>{{ detailData.skuList.length }} 条</span>
        </div>
        <el-table :data="detailData.skuList" border size="small">
          <el-table-column
            prop="displayCode"
            label="SKU 编码"
            min-width="170"
          />
          <el-table-column prop="displayBarcode" label="条码" min-width="150" />
          <el-table-column label="规格组合" min-width="220">
            <template #default="{ row }">
              <div class="goods-sku-specs">
                <span class="goods-sku-specs__summary">
                  {{ row.specSummary || "-" }}
                </span>
                <div v-if="row.specList?.length" class="goods-sku-specs__tags">
                  <el-tag
                    v-for="(spec, index) in getVisibleSkuSpecs(row.specList)"
                    :key="`${row.id}-${spec.specName}-${index}`"
                    size="small"
                    effect="plain"
                    round
                  >
                    {{ spec.specName }}: {{ spec.specValue }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="售价/起批价" min-width="100">
            <template #default="{ row }">¥ {{ row.price ?? "-" }}</template>
          </el-table-column>
          <el-table-column label="成本价" min-width="100">
            <template #default="{ row }">¥ {{ row.cost ?? "-" }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="库存" min-width="90" />
          <el-table-column label="重量" min-width="100">
            <template #default="{ row }">{{ row.weight ?? 0 }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section
        v-if="detailData?.wholesaleList?.length"
        class="goods-detail__section"
      >
        <div class="goods-detail__section-head">
          <h4>批发规则</h4>
          <span>{{ detailData.wholesaleList.length }} 档</span>
        </div>
        <el-table :data="detailData.wholesaleList" border size="small">
          <el-table-column
            prop="skuSpecText"
            label="对应 SKU"
            min-width="180"
          />
          <el-table-column prop="num" label="起批量" min-width="100" />
          <el-table-column label="批发价" min-width="120">
            <template #default="{ row }">¥ {{ row.price ?? "-" }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section
        v-if="detailData?.intro || detailData?.mobileIntro"
        class="goods-detail__section"
      >
        <div class="goods-detail__section-head">
          <h4>图文详情</h4>
          <span>SPU 详情内容</span>
        </div>
        <div class="goods-richtext-grid">
          <div v-if="detailData?.intro" class="goods-richtext">
            <h5>PC 详情</h5>
            <div class="goods-richtext__content" v-html="detailData.intro" />
          </div>
          <div v-if="detailData?.mobileIntro" class="goods-richtext">
            <h5>移动端详情</h5>
            <div
              class="goods-richtext__content"
              v-html="detailData.mobileIntro"
            />
          </div>
        </div>
      </section>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="goodsUnitVisible" title="商品单位管理" width="720px">
    <div class="goods-unit-panel">
      <el-alert
        title="本页单位管理固定调用管理端 /manager/goods/goodsUnit，不依赖 seller-api /store/**。"
        type="info"
        :closable="false"
      />
      <div class="goods-unit-panel__toolbar">
        <el-button type="primary" @click="openGoodsUnitCreate">
          新增单位
        </el-button>
      </div>
      <el-table v-loading="goodsUnitLoading" :data="goodsUnitRows" border>
        <el-table-column prop="name" label="单位名称" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openGoodsUnitEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleGoodsUnitDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="goodsUnitVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="goodsUnitFormVisible"
    :title="goodsUnitEditingId ? '编辑商品单位' : '新增商品单位'"
    width="460px"
    append-to-body
  >
    <el-form label-width="88px">
      <el-form-item label="单位名称" required>
        <el-input
          v-model="goodsUnitForm.name"
          maxlength="5"
          show-word-limit
          placeholder="请输入单位名称，例如 件 / 箱 / 瓶"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="goodsUnitFormVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="goodsUnitSaving"
        @click="submitGoodsUnit"
      >
        保存
      </el-button>
    </template>
  </el-dialog>

  <GoodsEditorDialog
    v-model="editorVisible"
    :mode="editorMode"
    :goods-data="editorData"
    @saved="handleEditorSaved"
  />
</template>

<style scoped>
:deep(.goods-thumb) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.goods-thumb__img) {
  width: 66px;
  height: 66px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #f1e5d9;
  background: #fff8f0;
}

:deep(.goods-thumb__placeholder) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  color: #b08b6b;
  font-size: 12px;
  background: #fff8f0;
  border: 1px solid #f1e5d9;
  border-radius: 12px;
}

:deep(.goods-title__name) {
  color: #30343a;
  font-weight: 600;
  line-height: 1.6;
  word-break: break-all;
}

:deep(.goods-title__sub) {
  margin-top: 6px;
  color: #8a8f97;
  font-size: 12px;
}

:deep(.money-text) {
  color: #ff6e18;
  font-weight: 700;
}

:deep(.money-text--profit) {
  color: #ff5b39;
}

.goods-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.goods-detail__hero {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.goods-detail__thumb {
  width: 108px;
  height: 108px;
  object-fit: cover;
  border: 1px solid #f1e5d9;
  border-radius: 16px;
  background: #fff8f0;
}

.goods-detail__thumb--empty {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #9a7b5b;
  font-size: 13px;
}

.goods-detail__summary {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.goods-detail__summary h3 {
  margin: 0;
  color: #2f3135;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.5;
}

.goods-detail__summary p {
  margin: 0;
  color: #8a8f97;
}

.goods-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.goods-unit-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goods-unit-panel__toolbar {
  display: flex;
  justify-content: flex-end;
}

.goods-manage__toolbar-button {
  color: #ff7a1a;
  background: #fff7ef;
  border: 1px solid #ffd1ab;
}

.goods-manage__toolbar-button:hover,
.goods-manage__toolbar-button:focus {
  color: #ff7a1a;
  background: #fff1e2;
  border-color: #ffbe86;
}

.goods-detail__section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  background: #fffaf5;
  border: 1px solid #f2e4d6;
  border-radius: 16px;
}

.goods-detail__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.goods-detail__section-head h4,
.goods-richtext h5 {
  margin: 0;
  color: #2f3135;
  font-size: 16px;
  font-weight: 700;
}

.goods-detail__section-head span {
  color: #9a7b5b;
  font-size: 12px;
}

.goods-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 14px;
}

.goods-gallery__item {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border: 1px solid #f1e5d9;
  border-radius: 14px;
  background: #fff;
}

.goods-sku-specs {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.goods-sku-specs__summary {
  color: #30343a;
  font-weight: 600;
  line-height: 1.5;
}

.goods-sku-specs__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.goods-richtext-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.goods-richtext {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.goods-richtext__content {
  max-height: 320px;
  padding: 14px;
  overflow: auto;
  color: #4a4f57;
  background: #fff;
  border: 1px solid #f1e5d9;
  border-radius: 14px;
}

.goods-richtext__content :deep(img) {
  max-width: 100%;
  height: auto;
}
</style>
