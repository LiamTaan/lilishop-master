<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getCardCouponGoodsPage } from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "CardCouponGoodsManage"
});

const data = ref<Record<string, any>[]>([]);
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
  const params: Record<string, any> = {};
  const res = await getCardCouponGoodsPage(params);
  data.value = extractApiRecords(res).map(normalizeCardCouponGoodsRecord);
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

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="卡券商品治理"
    description="承接卡券商品绑定、列表查询与治理动作。"
    api-path="/manager/promotion/cardCouponGoods"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="quickActions"
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
  >
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
  </WholesaleAdminPage>
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
