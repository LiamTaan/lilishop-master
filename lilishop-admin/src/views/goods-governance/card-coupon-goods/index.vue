<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  deleteCardCouponGoods,
  getCardCouponGoodsPage
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "CardCouponGoodsManage"
});

const data = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  couponType: "",
  goodsName: "",
  receiveLimit: ""
});

const filteredData = computed(() =>
  data.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.cardCouponName).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status.toUpperCase()
      : true;
    const typeMatched = extraFilters.couponType
      ? String(item.cardCouponType).includes(extraFilters.couponType)
      : true;
    const goodsMatched = extraFilters.goodsName
      ? String(item.goodsName).includes(extraFilters.goodsName)
      : true;
    const limitMatched = extraFilters.receiveLimit
      ? String(item.receiveLimit).includes(extraFilters.receiveLimit)
      : true;
    return (
      keywordMatched &&
      statusMatched &&
      typeMatched &&
      goodsMatched &&
      limitMatched
    );
  })
);
const selectedIds = computed(() =>
  [...new Set(selectedRows.value.map(item => String(item.id || "")).filter(Boolean))]
);
const pageColumns = computed<TableColumnList>(() => [
  ...columns,
  {
    label: "操作",
    prop: "operation",
    fixed: "right",
    width: 160,
    slot: "operation"
  }
]);

const summaryCards = computed(() => {
  const total = filteredData.value.length;
  const onlineCount = filteredData.value.filter(item =>
    String(item.promotionStatus).toUpperCase() === "START"
  ).length;
  const totalFaceValue = filteredData.value.reduce(
    (sum, item) => sum + Number(item.faceValueAmount || 0),
    0
  );
  return [
    {
      label: "卡券商品数",
      value: total,
      accent: "orange" as const,
      hint: "当前筛选结果"
    },
    {
      label: "进行中卡券",
      value: onlineCount,
      accent: "green" as const,
      hint: "当前有效促销"
    },
    {
      label: "面值总额",
      value: `¥ ${totalFaceValue}`,
      accent: "blue" as const,
      hint: "运营投放面值统计"
    },
    {
      label: "治理接口",
      value: "已接入",
      accent: "purple" as const,
      hint: "/manager/promotion/cardCouponGoods"
    }
  ];
});

const quickActions = [
  { label: "卡券类型", value: "已筛选", type: "warning" as const },
  { label: "适用商品", value: "已展示", type: "success" as const },
  { label: "状态治理", value: "已接入", type: "primary" as const },
  { label: "投放上限", value: "已展示", type: "info" as const }
];

function normalizeCardCouponGoodsRecord(item: Record<string, any>) {
  const coupon = item.coupon || {};
  const promotionStatus = coupon.promotionStatus || item.promotionStatus || "-";
  return {
    ...item,
    id: item.id || item.goodsId || item.couponId,
    cardCouponName:
      item.cardCouponName || coupon.couponName || item.name || item.goodsName || "-",
    cardCouponCode:
      item.cardCouponCode || coupon.id || item.couponCode || item.sn || item.goodsSn || "-",
    cardCouponType:
      item.cardCouponType ||
      coupon.couponTypeLabel ||
      coupon.couponType ||
      item.couponType ||
      "-",
    faceValueAmount:
      item.faceValueAmount ||
      coupon.price ||
      coupon.couponDiscount ||
      item.price ||
      item.amount ||
      0,
    faceValue:
      item.faceValueText ||
      coupon.faceValueText ||
      item.faceValue ||
      coupon.price ||
      item.price ||
      item.amount ||
      "-",
    goodsName: item.goodsName || item.bindGoodsName || item.scopeName || "-",
    stock:
      item.stockText || coupon.stockText || item.stockNum || coupon.stockNum || item.stock || item.quantity || 0,
    receiveLimit:
      item.receiveLimit || coupon.couponLimitNum || item.buyLimit || item.exchangeLimit || "-",
    promotionStatus,
    promotionStatusLabel:
      coupon.promotionStatusLabel ||
      item.promotionStatusLabel ||
      getPromotionStatusLabel(promotionStatus)
  };
}

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  const res = await getCardCouponGoodsPage(params);
  data.value = extractApiRecords(res).map(normalizeCardCouponGoodsRecord);
}

function handleSelectionChange(selection: Record<string, any>[]) {
  selectedRows.value = selection;
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.couponType = "";
  extraFilters.goodsName = "";
  extraFilters.receiveLimit = "";
  loadData();
}

function openDetail(row: Record<string, any>) {
  detailRow.value = row;
  detailVisible.value = true;
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先选择要删除的卡券商品", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除选中的 ${selectedIds.value.length} 个卡券商品关联吗？`,
    "删除确认",
    {
      type: "warning"
    }
  );
  try {
    await deleteCardCouponGoods(selectedIds.value);
    message("卡券商品批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("卡券商品批量删除失败", { type: "error" });
  }
}

async function handleSingleDelete(row: Record<string, any>) {
  selectedRows.value = [row];
  await handleBatchDelete();
}

function exportCardCouponGoods() {
  if (!filteredData.value.length) {
    message("暂无可导出的卡券商品", { type: "warning" });
    return;
  }
  const worksheet = utils.json_to_sheet(
    filteredData.value.map(item => ({
      卡券名称: item.cardCouponName,
      卡券编码: item.cardCouponCode,
      卡券类型: item.cardCouponType,
      适用商品: item.goodsName,
      面值: item.faceValue,
      库存: item.stock,
      领取上限: item.receiveLimit,
      状态: item.promotionStatusLabel
    }))
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "卡券商品");
  writeFile(workbook, "卡券商品.xlsx");
  message("卡券商品导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="卡券商品治理"
    description="承接卡券商品绑定、列表查询与治理动作。"
    api-path="/manager/promotion/cardCouponGoods"
    :columns="pageColumns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="quickActions"
    :selectable="true"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' },
      { label: '已关闭', value: 'CLOSE' }
    ]"
    keyword-label="卡券名称"
    keyword-placeholder="请输入卡券名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button :disabled="!filteredData.length" @click="exportCardCouponGoods">
        导出
      </el-button>
      <el-button
        type="danger"
        :disabled="!selectedIds.length"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="卡券类型">
        <el-input
          v-model="extraFilters.couponType"
          placeholder="请输入卡券类型"
          clearable
        />
      </el-form-item>
      <el-form-item label="适用商品">
        <el-input
          v-model="extraFilters.goodsName"
          placeholder="请输入适用商品"
          clearable
        />
      </el-form-item>
      <el-form-item label="领取上限">
        <el-input
          v-model="extraFilters.receiveLimit"
          placeholder="请输入领取上限"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleSingleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="卡券商品详情" size="42%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="卡券名称">
        {{ detailRow.cardCouponName }}
      </el-descriptions-item>
      <el-descriptions-item label="卡券编码">
        {{ detailRow.cardCouponCode }}
      </el-descriptions-item>
      <el-descriptions-item label="卡券类型">
        {{ detailRow.cardCouponType }}
      </el-descriptions-item>
      <el-descriptions-item label="适用商品">
        {{ detailRow.goodsName }}
      </el-descriptions-item>
      <el-descriptions-item label="面值">
        {{ detailRow.faceValue }}
      </el-descriptions-item>
      <el-descriptions-item label="库存">
        {{ detailRow.stock }}
      </el-descriptions-item>
      <el-descriptions-item label="领取上限">
        {{ detailRow.receiveLimit }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        {{ detailRow.promotionStatusLabel }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
:deep(.biz-title__main) {
  color: #30343a;
  font-weight: 600;
  line-height: 1.6;
}

:deep(.biz-title__sub) {
  margin-top: 6px;
  color: #8a8f97;
  font-size: 12px;
}

:deep(.money-text) {
  color: #ff6e18;
  font-weight: 700;
}
</style>
