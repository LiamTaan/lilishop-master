<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import { getOrderDetail, getOrderPage } from "@/api/order-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getFulfillmentTypeLabel,
  getPrimaryActionLabel,
  getOrderStatusLabel,
  getOrderTypeLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "OrderManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  storeKeyword: "",
  orderTypeValue: "",
  goodsKeyword: ""
});

function normalizeOrderRecord(item: Record<string, any>) {
  const source =
    item.order && typeof item.order === "object" ? item.order : item;
  const orderItems = Array.isArray(item.orderItems)
    ? item.orderItems
    : Array.isArray(source.orderItems)
      ? source.orderItems
      : [];
  const goodsSummary = orderItems.length
    ? orderItems
        .map(goods => goods?.name || goods?.goodsName)
        .filter(Boolean)
        .join(" / ")
    : source.goodsName || source.skuName || "-";
  return {
    ...source,
    ...item,
    id: source.id || source.orderSn || source.sn,
    orderSn: source.orderSn || source.sn || "-",
    storeName: source.storeName || source.storeNickName || "-",
    memberName: source.memberName || source.buyerName || "-",
    orderStatus: source.orderStatus || source.status || "-",
    displayStatus:
      item.displayStatus || source.displayStatus || getOrderStatusLabel(source.orderStatus || source.status || "-"),
    orderType: source.orderType || source.orderSource || "-",
    fulfillmentType:
      item.fulfillmentType || source.fulfillmentType || "LOGISTICS",
    primaryAction: item.primaryAction || source.primaryAction || "NONE",
    primaryActionEnabled:
      item.primaryActionEnabled ?? source.primaryActionEnabled ?? false,
    verificationCode:
      item.verificationCode || source.verificationCode || source.order?.verificationCode || "-",
    storeAddressPath:
      item.storeAddressPath || source.storeAddressPath || source.order?.storeAddressPath || "-",
    storeAddressMobile:
      item.storeAddressMobile || source.storeAddressMobile || source.order?.storeAddressMobile || "-",
    orderPromotionType:
      source.orderPromotionType || source.promotionType || "",
    orderTypeDisplay:
      source.orderPromotionType ||
      source.orderType ||
      source.orderSource ||
      "-",
    goodsSummary,
    payableAmount:
      source.flowPrice ??
      source.order?.flowPrice ??
      source.payableAmount ??
      source.price ??
      source.totalPrice ??
      "-",
    paymentMethod:
      item.paymentMethodValue ||
      source.paymentMethodValue ||
      source.paymentMethod ||
      "-",
    paymentTime: source.paymentTime || "-",
    createTime: source.createTime || source.orderTime || source.updateTime || "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item => {
    const storeMatched = extraFilters.storeKeyword
      ? String(item.storeName).includes(extraFilters.storeKeyword)
      : true;
    const orderTypeMatched = extraFilters.orderTypeValue
      ? String(item.orderTypeDisplay).toUpperCase() === extraFilters.orderTypeValue
      : true;
    const goodsMatched = extraFilters.goodsKeyword
      ? String(item.goodsSummary || "").includes(extraFilters.goodsKeyword)
      : true;
    return storeMatched && orderTypeMatched && goodsMatched;
  })
);

const summaryCards = computed(() => {
  const totalAmount = filteredData.value.reduce(
    (sum, item) => sum + Number(item.payableAmount || 0),
    0
  );
  return [
    { label: "订单总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
    {
      label: "待处理订单",
      value: filteredData.value.filter(item => String(item.orderStatus).toUpperCase().includes("WAIT")).length,
      accent: "blue" as const,
      hint: "待付款/待发货/待核销"
    },
    { label: "实付金额", value: totalAmount, accent: "green" as const, hint: "筛选结果汇总" },
    { label: "订单治理", value: "已接入", accent: "purple" as const, hint: "状态与金额已展示" }
  ];
});

const detailItems = computed(() => [
  { label: "订单号", value: detail.value.orderSn || "-" },
  { label: "店铺名称", value: detail.value.storeName || "-" },
  { label: "下单用户", value: detail.value.memberName || "-" },
  { label: "商品信息", value: detail.value.goodsSummary || "-" },
  {
    label: "展示状态",
    value:
      detail.value.displayStatus || getOrderStatusLabel(detail.value.orderStatus)
  },
  {
    label: "订单类型",
    value: getOrderTypeLabel(detail.value.orderTypeDisplay || detail.value.orderType)
  },
  {
    label: "履约类型",
    value: getFulfillmentTypeLabel(detail.value.fulfillmentType)
  },
  {
    label: "主操作",
    value: `${getPrimaryActionLabel(detail.value.primaryAction)}${
      detail.value.primaryActionEnabled ? "" : "（不可执行）"
    }`
  },
  {
    label: "提货码",
    value:
      detail.value.primaryAction === "VERIFY" || detail.value.verificationCode !== "-"
        ? detail.value.verificationCode || "-"
        : "-"
  },
  { label: "自提点地址", value: detail.value.storeAddressPath || "-" },
  { label: "自提点电话", value: detail.value.storeAddressMobile || "-" },
  { label: "实付金额", value: `¥ ${detail.value.payableAmount || "-"}` },
  { label: "支付方式", value: detail.value.paymentMethod || "-" },
  { label: "支付时间", value: detail.value.paymentTime || "-" },
  { label: "创建时间", value: detail.value.createTime || "-" }
]);

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10
  };
  if (query.keyword) params.orderSn = query.keyword;
  if (query.status) params.orderStatus = query.status;
  const res = await getOrderPage(params);
  data.value = extractApiRecords(res).map(normalizeOrderRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.storeKeyword = "";
  extraFilters.orderTypeValue = "";
  extraFilters.goodsKeyword = "";
  loadData();
}

async function openDetail(row: Record<string, any>) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getOrderDetail(String(row.orderSn));
    detail.value = normalizeOrderRecord(extractApiPayload(res) || row);
  } catch (_error) {
    detail.value = row;
    message("订单详情加载失败，已展示列表基础信息", { type: "warning" });
  } finally {
    detailLoading.value = false;
  }
}

function exportOrders() {
  if (!filteredData.value.length) {
    message("暂无可导出的订单数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    订单号: item.orderSn,
    商品信息: item.goodsSummary,
    店铺名称: item.storeName,
    下单用户: item.memberName,
    订单状态: getOrderStatusLabel(item.orderStatus),
    订单类型: getOrderTypeLabel(item.orderTypeDisplay),
    履约类型: getOrderTypeLabel(item.orderType),
    实付金额: item.payableAmount,
    支付方式: item.paymentMethod,
    支付时间: item.paymentTime,
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "订单列表");
  writeFile(workbook, "订单列表.xlsx");
  message("订单数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="订单列表"
    description="承接平台订单查询、详情查看、订单金额与导出治理。"
    api-path="/manager/order/order"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待付款', value: 'UNPAID' },
      { label: '已付款', value: 'PAID' },
      { label: '待发货', value: 'UNDELIVERED' },
      { label: '已发货', value: 'DELIVERED' },
      { label: '待核销', value: 'TAKE' },
      { label: '待核销(自提)', value: 'STAY_PICKED_UP' },
      { label: '已完成', value: 'COMPLETED' },
      { label: '已关闭', value: 'CANCELLED' }
    ]"
    :quick-actions="[
      { label: '订单详情', value: '已接入', type: 'warning' },
      { label: '店铺筛选', value: '前端补充', type: 'success' },
      { label: '金额导出', value: '已接入', type: 'primary' }
    ]"
    keyword-label="订单号"
    keyword-placeholder="请输入订单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportOrders">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="店铺名称">
        <el-input
          v-model="extraFilters.storeKeyword"
          placeholder="请输入店铺名称"
          clearable
        />
      </el-form-item>
      <el-form-item label="订单类型">
        <el-select
          v-model="extraFilters.orderTypeValue"
          placeholder="请选择订单类型"
          clearable
        >
          <el-option label="普通订单" value="NORMAL" />
          <el-option label="拼团订单" value="PINTUAN" />
          <el-option label="批发订单" value="WHOLESALE" />
          <el-option label="积分订单" value="POINTS" />
          <el-option label="赠品订单" value="GIFT" />
          <el-option label="砍价订单" value="KANJIA" />
          <el-option label="虚拟订单" value="VIRTUAL" />
          <el-option label="电子卡券订单" value="E_COUPON" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品信息">
        <el-input
          v-model="extraFilters.goodsKeyword"
          placeholder="请输入商品名称"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="订单详情" size="40%">
    <el-descriptions v-loading="detailLoading" :column="1" border>
      <el-descriptions-item
        v-for="item in detailItems"
        :key="item.label"
        :label="item.label"
      >
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
