<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import { getCategoryPage, getWholesaleSkuPage } from "@/api/goods-governance";
import {
  createCoupon,
  deleteCoupons,
  getCouponDetail,
  getCouponMembers,
  getCouponPage,
  updateCoupon
} from "@/api/marketing-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";
import {
  createPromotionGoodsItem,
  formatDateTimeForField,
  formatMoney,
  normalizePromotionDetail,
  promotionScopeOptions,
  toNumber
} from "../shared/promotion-helpers";

defineOptions({
  name: "CouponManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const memberRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const memberVisible = ref(false);
const saving = ref(false);
const skuLoading = ref(false);
const categoryLoading = ref(false);
const editingId = ref("");
const currentRow = ref<Record<string, any> | null>(null);
const editingBase = ref<Record<string, any>>({});
const skuOptions = ref<
  Array<{
    label: string;
    value: string;
    skuId: string;
    goodsId: string;
    goodsName: string;
    thumbnail: string;
    originalPrice: number;
    quantity: number;
  }>
>([]);
const categoryOptions = ref<Array<{ label: string; value: string }>>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const couponForm = reactive(createCouponForm());

function getCouponRangeTypeLabel(value: unknown) {
  const normalized = String(value || "")
    .trim()
    .toUpperCase();
  if (normalized === "FIXEDTIME") return "固定时间段";
  if (normalized === "DYNAMICTIME") return "领券后按天生效";
  return String(value || "-");
}

function getCouponIssueStatusLabel(value: unknown) {
  const normalized = String(value || "")
    .trim()
    .toUpperCase();
  if (normalized === "NEW") return "未开始发放";
  if (normalized === "START") return "发放中";
  if (normalized === "END") return "发放结束";
  if (normalized === "CLOSE" || normalized === "CLOSED") return "已关闭";
  return getPromotionStatusLabel(value);
}

function getCouponValiditySummary(row: Record<string, any>) {
  if (String(row.rangeDayType || "").toUpperCase() === "DYNAMICTIME") {
    return `领券后 ${toNumber(row.effectiveDays || 0)} 天内有效`;
  }
  return `${row.startTimeText || "-"} 至 ${row.endTimeText || "-"}`;
}

const columns: TableColumnList = [
  { label: "优惠券名称", prop: "couponName", minWidth: 180 },
  { label: "活动名称", prop: "promotionName", minWidth: 180 },
  { label: "优惠类型", prop: "couponType", minWidth: 120 },
  { label: "面额/折扣", prop: "amountDisplay", minWidth: 120 },
  { label: "发放数量", prop: "publishNum", minWidth: 100 },
  { label: "已领/已用", prop: "receivedUsedText", minWidth: 120 },
  { label: "发放状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 250, slot: "operation" }
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

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const summaryCards = computed(() => [
  { label: "优惠券数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "发放中",
    value: filteredRows.value.filter(item => String(item.promotionStatus).toUpperCase() === "START").length,
    accent: "green" as const,
    hint: "当前可领取优惠券"
  },
  {
    label: "已领数量",
    value: filteredRows.value.reduce((sum, item) => sum + Number(item.receivedNum || 0), 0),
    accent: "blue" as const,
    hint: "累计已领取"
  },
  { label: "治理动作", value: "增改删", accent: "purple" as const, hint: "支持新增、编辑、删除" }
]);

const detailFields = computed(() => {
  if (!currentRow.value) {
    return [];
  }
  const detail = currentRow.value;
  return [
    { label: "活动名称", value: detail.promotionName || "-" },
    { label: "优惠券名称", value: detail.couponName || "-" },
    { label: "优惠类型", value: detail.couponTypeLabel || detail.couponType || "-" },
    { label: "领取方式", value: detail.getType || "-" },
    {
      label: "面额/折扣",
      value:
        detail.couponTypeValue === "DISCOUNT"
          ? `${toNumber(detail.couponDiscount)} 折`
          : formatMoney(detail.price)
    },
    { label: "消费门槛", value: formatMoney(detail.consumeThreshold) },
    { label: "发放数量", value: detail.publishNum || 0 },
    { label: "限领数量", value: detail.couponLimitNum || 0 },
    { label: "活动范围", value: detail.scopeType || "-" },
    {
      label: "适用范围",
      value:
        detail.scopeType === "PORTION_GOODS" && Array.isArray(detail.promotionGoodsList)
          ? detail.promotionGoodsList
              .map((item: Record<string, any>) => item.goodsName || item.skuId)
              .filter(Boolean)
              .join("、") || "-"
          : detail.scopeType === "PORTION_GOODS_CATEGORY"
            ? detail.scopeId || "-"
            : detail.scopeId || "-"
    },
    { label: "生效规则", value: getCouponRangeTypeLabel(detail.rangeDayType) },
    {
      label: "有效说明",
      value: getCouponValiditySummary(detail)
    },
    { label: "发放状态", value: detail.promotionStatusLabel || "-" },
    { label: "创建时间", value: detail.createTimeText || "-" },
    { label: "活动描述", value: detail.description || "-" }
  ];
});

function normalizeSkuOption(item: Record<string, any>) {
  const skuId = String(item.id || item.skuId || "").trim();
  const goodsName = item.goodsName || item.name || "未命名商品";
  const goodsId = String(item.goodsId || "").trim();
  const simpleSpecs = item.simpleSpecs ? ` / ${item.simpleSpecs}` : "";
  return {
    label: `${goodsName}${simpleSpecs}`,
    value: skuId,
    skuId,
    goodsId,
    goodsName,
    thumbnail: item.thumbnail || "",
    originalPrice: toNumber(item.price || item.originalPrice),
    quantity: toNumber(item.quantity)
  };
}

function mergeSkuOptions(list: Record<string, any>[]) {
  const optionMap = new Map(skuOptions.value.map(item => [item.value, item] as const));
  list
    .map(normalizeSkuOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  skuOptions.value = Array.from(optionMap.values());
}

async function searchPromotionSkus(keyword: string) {
  skuLoading.value = true;
  try {
    const response = await getWholesaleSkuPage({
      pageNumber: 1,
      pageSize: 50,
      salesModel: "WHOLESALE",
      goodsName: keyword.trim() || undefined
    });
    mergeSkuOptions(extractApiRecords(response));
  } catch (_error) {
    message("商品搜索失败", { type: "error" });
  } finally {
    skuLoading.value = false;
  }
}

async function ensureCategoryOptions() {
  if (categoryOptions.value.length) return;
  categoryLoading.value = true;
  try {
    const response = await getCategoryPage({ pageNumber: 1, pageSize: 500 });
    categoryOptions.value = extractApiRecords(response).map((item: Record<string, any>) => ({
      label: item.categoryName || item.name || String(item.id || ""),
      value: String(item.id || "")
    }));
  } catch (_error) {
    message("分类加载失败", { type: "error" });
  } finally {
    categoryLoading.value = false;
  }
}

function handlePromotionSkuChange(item: Record<string, any>, skuId: string) {
  const target = skuOptions.value.find(option => option.value === skuId);
  item.skuId = skuId;
  item.scopeId = skuId;
  item.goodsId = target?.goodsId || "";
  item.goodsName = target?.goodsName || "";
  item.thumbnail = target?.thumbnail || "";
  if (!item.originalPrice) {
    item.originalPrice = target?.originalPrice || 0;
  }
  if (!item.quantity) {
    item.quantity = target?.quantity || 0;
  }
}

function createCouponForm() {
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
    effectiveDays: 1,
    startTime: "",
    endTime: "",
    scopeType: "PORTION_GOODS",
    scopeId: "",
    promotionGoodsList: [createPromotionGoodsItem()]
  };
}

function resetCouponForm() {
  Object.assign(couponForm, createCouponForm());
}

function fillCouponForm(source: Record<string, any>) {
  Object.assign(couponForm, {
    promotionName: source.promotionName || "平台优惠券",
    couponName: source.couponName || "",
    couponType: source.couponType || "PRICE",
    price: toNumber(source.price || 0),
    couponDiscount: toNumber(source.couponDiscount || 0),
    getType: source.getType || "FREE_GET",
    publishNum: toNumber(source.publishNum || 0),
    couponLimitNum: toNumber(source.couponLimitNum || 0),
    consumeThreshold: toNumber(source.consumeThreshold || 0),
    description: source.description || "",
    rangeDayType: source.rangeDayType || "FIXEDTIME",
    effectiveDays: toNumber(source.effectiveDays || 1),
    startTime: formatDateTimeForField(source.startTime),
    endTime: formatDateTimeForField(source.endTime),
    scopeType: source.scopeType || "PORTION_GOODS",
    scopeId: source.scopeId || "",
    promotionGoodsList: Array.isArray(source.promotionGoodsList) && source.promotionGoodsList.length > 0
      ? source.promotionGoodsList.map((item: Record<string, any>) => createPromotionGoodsItem(item))
      : [createPromotionGoodsItem()]
  });
  mergeSkuOptions(couponForm.promotionGoodsList);
}

function buildCouponPayload() {
  const promotionGoodsList =
    couponForm.scopeType === "PORTION_GOODS"
      ? couponForm.promotionGoodsList.map(item => ({
          ...item,
          skuId: item.skuId || item.scopeId,
          scopeId: item.skuId || item.scopeId,
          price: toNumber(item.price),
          originalPrice: toNumber(item.originalPrice),
          quantity: toNumber(item.quantity),
          limitNum: toNumber(item.limitNum),
          points: toNumber(item.points),
          scopeType: "PORTION_GOODS"
        }))
      : [];

  const payload: Record<string, any> = {
    ...editingBase.value,
    ...couponForm,
    id: editingId.value || editingBase.value.id,
    price: toNumber(couponForm.price),
    couponDiscount: toNumber(couponForm.couponDiscount),
    publishNum: toNumber(couponForm.publishNum),
    couponLimitNum: toNumber(couponForm.couponLimitNum),
    consumeThreshold: toNumber(couponForm.consumeThreshold),
    effectiveDays: couponForm.rangeDayType === "DYNAMICTIME" ? toNumber(couponForm.effectiveDays) : null,
    startTime: couponForm.rangeDayType === "FIXEDTIME" ? couponForm.startTime : null,
    endTime: couponForm.rangeDayType === "FIXEDTIME" ? couponForm.endTime : null,
    scopeId:
      couponForm.scopeType === "ALL"
        ? ""
        : couponForm.scopeType === "PORTION_GOODS"
          ? promotionGoodsList
              .map(item => item.skuId)
              .filter(Boolean)
              .join(",")
          : couponForm.scopeId,
    promotionGoodsList
  };
  return payload;
}

function normalizeRow(item: Record<string, any>) {
  const price = Number(item.price ?? 0);
  const couponDiscount = Number(item.couponDiscount ?? 0);
  const couponTypeValue = item.couponType || item.couponTypeValue || "-";
  return {
    ...normalizePromotionDetail(item),
    couponTypeValue,
    promotionStatusLabel: getCouponIssueStatusLabel(item.promotionStatus || item.status || "-"),
    couponTypeLabel: item.couponTypeLabel || couponTypeValue,
    promotionName: item.promotionName || item.couponName || "-",
    couponName: item.couponName || "-",
    couponType: item.couponTypeLabel || couponTypeValue,
    amountDisplay:
      item.faceValueText ||
      (couponTypeValue === "DISCOUNT" ? `${couponDiscount} 折` : `¥ ${price.toFixed(2)}`),
    publishNum: Number(item.publishNum ?? 0),
    receivedNum: Number(item.receivedNum ?? 0),
    usedNum: Number(item.usedNum ?? 0),
    receivedUsedText: `${Number(item.receivedNum ?? 0)} / ${Number(item.usedNum ?? 0)}`
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  editingId.value = "";
  editingBase.value = {};
  resetCouponForm();
  searchPromotionSkus("");
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getCouponDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    editingId.value = String(detail.id || row.id);
    editingBase.value = detail;
    fillCouponForm(detail);
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
  saving.value = true;
  try {
    const payload = buildCouponPayload();
    const response = editingId.value
      ? await updateCoupon(payload)
      : await createCoupon(payload);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "优惠券保存失败");
    }
    if (editingId.value) {
      message("优惠券更新成功", { type: "success" });
    } else {
      message("优惠券创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    message(
      error instanceof Error && error.message
        ? error.message
        : "优惠券保存失败，请检查表单内容",
      { type: "error" }
    );
  } finally {
    saving.value = false;
  }
}

function addGoodsItem() {
  couponForm.promotionGoodsList.push(createPromotionGoodsItem());
}

function removeGoodsItem(index: number) {
  if (couponForm.promotionGoodsList.length === 1) {
    couponForm.promotionGoodsList.splice(0, 1, createPromotionGoodsItem());
    return;
  }
  couponForm.promotionGoodsList.splice(index, 1);
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

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的优惠券", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 张优惠券吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deleteCoupons(selectedIds.value);
    selectedRows.value = [];
    message("优惠券批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("优惠券批量删除失败", { type: "error" });
  }
}

function exportCoupons() {
  if (!filteredRows.value.length) {
    message("暂无可导出的优惠券数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    优惠券名称: item.couponName,
    活动名称: item.promotionName,
    优惠类型: item.couponType,
    面额折扣: item.amountDisplay,
    发放数量: item.publishNum,
    已领数量: item.receivedNum,
    已用数量: item.usedNum,
    发放状态: item.promotionStatusLabel || getCouponIssueStatusLabel(item.promotionStatus)
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "优惠券管理");
  writeFile(workbook, "优惠券管理.xlsx");
  message("优惠券数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="优惠券管理"
    description="承接平台优惠券的列表、详情、编辑、删除和领取明细查看。"
    api-path="/manager/promotion/coupon"
    :columns="columns"
    :data="filteredRows"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '发放中', value: 'START' },
      { label: '发放结束', value: 'END' }
    ]"
    status-label="发放状态"
    keyword-label="优惠券"
    keyword-placeholder="请输入优惠券名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportCoupons">导出</el-button>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="primary" @click="openMembers(row)">领取明细</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑优惠券' : '新增优惠券'"
    width="980px"
  >
    <el-form label-width="110px" class="promotion-form">
      <div class="form-grid">
        <el-form-item label="活动名称">
          <el-input v-model="couponForm.promotionName" />
        </el-form-item>
        <el-form-item label="优惠券名称">
          <el-input v-model="couponForm.couponName" />
        </el-form-item>
        <el-form-item label="优惠类型">
          <el-select v-model="couponForm.couponType">
            <el-option label="减免现金" value="PRICE" />
            <el-option label="折扣券" value="DISCOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="couponForm.couponType === 'DISCOUNT' ? '折扣值' : '面额'">
          <el-input-number
            v-model="couponForm[couponForm.couponType === 'DISCOUNT' ? 'couponDiscount' : 'price']"
            :min="0"
            :max="couponForm.couponType === 'DISCOUNT' ? 10 : 999999"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="领取方式">
          <el-select v-model="couponForm.getType">
            <el-option label="免费领取" value="FREE_GET" />
            <el-option label="活动赠送" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放数量">
          <el-input-number v-model="couponForm.publishNum" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="限领数量">
          <el-input-number v-model="couponForm.couponLimitNum" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消费门槛">
          <el-input-number
            v-model="couponForm.consumeThreshold"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生效规则">
          <el-select v-model="couponForm.rangeDayType">
            <el-option label="固定时间段" value="FIXEDTIME" />
            <el-option label="领券后按天生效" value="DYNAMICTIME" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="couponForm.rangeDayType === 'DYNAMICTIME'" label="有效天数">
          <el-input-number v-model="couponForm.effectiveDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="发放开始时间">
          <el-date-picker
            v-model="couponForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="couponForm.rangeDayType === 'FIXEDTIME'" label="发放结束时间">
          <el-date-picker
            v-model="couponForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动范围">
          <el-select v-model="couponForm.scopeType">
            <el-option
              v-for="option in promotionScopeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="couponForm.scopeType === 'PORTION_GOODS_CATEGORY'"
          label="适用分类"
        >
          <el-select
            v-model="couponForm.scopeId"
            filterable
            clearable
            :loading="categoryLoading"
            placeholder="请选择适用分类"
            style="width: 100%"
            @visible-change="visible => visible && ensureCategoryOptions()"
          >
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-else-if="couponForm.scopeType !== 'ALL' && couponForm.scopeType !== 'PORTION_GOODS'"
          label="范围标识"
        >
          <el-input
            v-model="couponForm.scopeId"
            placeholder="请输入范围标识"
          />
        </el-form-item>
      </div>
      <el-form-item label="活动描述">
        <el-input v-model="couponForm.description" type="textarea" :rows="3" />
      </el-form-item>

      <section v-if="couponForm.scopeType === 'PORTION_GOODS'" class="goods-section">
        <div class="section-header">
          <span>关联商品</span>
          <el-button type="primary" link @click="addGoodsItem">新增商品</el-button>
        </div>
        <div
          v-for="(item, index) in couponForm.promotionGoodsList"
          :key="`coupon-goods-${index}`"
          class="goods-card"
        >
          <div class="section-header">
            <strong>商品 {{ index + 1 }}</strong>
            <el-button type="danger" link @click="removeGoodsItem(index)">移除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="选择商品">
              <el-select
                :model-value="item.skuId"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchPromotionSkus"
                :loading="skuLoading"
                placeholder="请输入商品名称搜索后选择 SKU"
                style="width: 100%"
                @update:model-value="value => handlePromotionSkuChange(item, String(value || ''))"
              >
                <el-option
                  v-for="option in skuOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="商品名称">
              <el-input v-model="item.goodsName" disabled />
            </el-form-item>
            <el-form-item label="缩略图">
              <el-input v-model="item.thumbnail" disabled />
            </el-form-item>
            <el-form-item label="促销价">
              <el-input-number v-model="item.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="原价">
              <el-input-number
                v-model="item.originalPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="促销库存">
              <el-input-number v-model="item.quantity" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="限购数量">
              <el-input-number v-model="item.limitNum" :min="0" style="width: 100%" />
            </el-form-item>
          </div>
        </div>
      </section>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="优惠券详情" size="56%">
    <el-descriptions v-if="currentRow" :column="2" border class="mb-4">
      <el-descriptions-item
        v-for="field in detailFields"
        :key="field.label"
        :label="field.label"
        :span="field.label === '活动描述' ? 2 : 1"
      >
        <div class="detail-text">{{ field.value }}</div>
      </el-descriptions-item>
    </el-descriptions>
    <section v-if="currentRow?.promotionGoodsList?.length" class="goods-section">
      <div class="section-header">
        <span>关联商品明细</span>
      </div>
      <pure-table
        row-key="skuId"
        :data="currentRow.promotionGoodsList"
        :columns="[
          { label: '商品名称', prop: 'goodsName', minWidth: 220 },
          { label: '促销价', prop: 'price', minWidth: 100 },
          { label: '原价', prop: 'originalPrice', minWidth: 100 },
          { label: '库存', prop: 'quantity', minWidth: 80 }
        ]"
      />
    </section>
  </el-drawer>

  <el-dialog v-model="memberVisible" title="优惠券领取明细" width="860px">
    <pure-table row-key="id" :data="memberRows" :columns="memberColumns" />
  </el-dialog>
</template>

<style scoped>
.promotion-form {
  padding-right: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.goods-section {
  margin-top: 12px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  background: #fafbfc;
}

.goods-card {
  padding: 16px;
  margin-top: 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #edf0f5;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
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
