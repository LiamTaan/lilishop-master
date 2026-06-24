<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getPointsGoodsPage } from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "PointsGoodsManage"
});

const data = ref<Record<string, any>[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  goodsCode: "",
  categoryName: "",
  exchangeLimit: ""
});

const filteredData = computed(() =>
  data.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.goodsName).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status.toUpperCase()
      : true;
    const codeMatched = extraFilters.goodsCode
      ? String(item.goodsCode).includes(extraFilters.goodsCode)
      : true;
    const categoryMatched = extraFilters.categoryName
      ? String(item.categoryName).includes(extraFilters.categoryName)
      : true;
    const limitMatched = extraFilters.exchangeLimit
      ? String(item.exchangeLimit).includes(extraFilters.exchangeLimit)
      : true;
    return keywordMatched && statusMatched && codeMatched && categoryMatched && limitMatched;
  })
);

const summaryCards = computed(() => {
  const total = filteredData.value.length;
  const activeCount = filteredData.value.filter(item =>
    String(item.promotionStatus).toUpperCase() === "START"
  ).length;
  const totalStock = filteredData.value.reduce(
    (sum, item) => sum + Number(item.stock || 0),
    0
  );
  return [
    {
      label: "积分商品数",
      value: total,
      accent: "orange" as const,
      hint: "当前筛选结果"
    },
    {
      label: "上架商品",
      value: activeCount,
      accent: "green" as const,
      hint: "当前已可兑换"
    },
    {
      label: "可兑库存",
      value: totalStock,
      accent: "blue" as const,
      hint: "用于运营盘点"
    },
    {
      label: "治理接口",
      value: "已接入",
      accent: "purple" as const,
      hint: "/manager/promotion/pointsGoods"
    }
  ];
});

const quickActions = [
  { label: "积分门槛", value: "已展示", type: "warning" as const },
  { label: "兑换上限", value: "已筛选", type: "success" as const },
  { label: "状态治理", value: "已接入", type: "primary" as const },
  { label: "店铺归属", value: "已展示", type: "info" as const }
];

function normalizePointsGoodsRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.goodsId,
    goodsName: item.goodsName || item.name || "-",
    goodsCode: item.goodsCode || item.sn || item.goodsSn || item.goodsNo || "-",
    categoryName:
      item.categoryName || item.categoryPathName || item.goodsCategoryName || "-",
    storeName: item.storeName || item.storeNickName || "平台积分商城",
    points: item.points || item.point || item.requirePoints || "-",
    stock: item.stock || item.quantity || 0,
    exchangeLimit: item.exchangeLimit || item.buyLimit || "-",
    promotionStatus:
      item.promotionStatus || item.status || item.marketEnable || "-",
    promotionStatusLabel:
      item.statusText ||
      item.statusName ||
      getPromotionStatusLabel(item.promotionStatus || item.status || item.marketEnable)
  };
}

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.goodsName = query.keyword;
  if (query.status) params.promotionStatus = query.status;
  const res = await getPointsGoodsPage(params);
  data.value = extractApiRecords(res).map(normalizePointsGoodsRecord);
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
  extraFilters.categoryName = "";
  extraFilters.exchangeLimit = "";
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="积分商品治理"
    description="承接积分商品查询、上下架与兑换配置。"
    api-path="/manager/promotion/pointsGoods"
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
    keyword-label="积分商品"
    keyword-placeholder="请输入积分商品名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="商品编码">
        <el-input
          v-model="extraFilters.goodsCode"
          placeholder="请输入商品编码"
          clearable
        />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-input
          v-model="extraFilters.categoryName"
          placeholder="请输入商品分类"
          clearable
        />
      </el-form-item>
      <el-form-item label="兑换上限">
        <el-input
          v-model="extraFilters.exchangeLimit"
          placeholder="请输入兑换上限"
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
