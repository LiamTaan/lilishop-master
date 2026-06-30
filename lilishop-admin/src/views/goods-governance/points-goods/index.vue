<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  deletePointsGoods,
  getPointsGoodsDetail,
  getPointsGoodsPage
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "PointsGoodsManage"
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
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  if (query.keyword) params.goodsName = query.keyword;
  if (query.status) params.promotionStatus = query.status;
  const res = await getPointsGoodsPage(params);
  data.value = extractApiRecords(res).map(normalizePointsGoodsRecord);
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
  extraFilters.goodsCode = "";
  extraFilters.categoryName = "";
  extraFilters.exchangeLimit = "";
  loadData();
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getPointsGoodsDetail(String(row.id));
    detailRow.value = normalizePointsGoodsRecord(
      extractApiPayload<Record<string, any>>(res) || row
    );
    detailVisible.value = true;
  } catch (_error) {
    message("积分商品详情加载失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先选择要删除的积分商品", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除选中的 ${selectedIds.value.length} 个积分商品吗？`,
    "删除确认",
    {
      type: "warning"
    }
  );
  try {
    await deletePointsGoods(selectedIds.value);
    message("积分商品批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("积分商品批量删除失败", { type: "error" });
  }
}

async function handleSingleDelete(row: Record<string, any>) {
  selectedRows.value = [row];
  await handleBatchDelete();
}

function exportPointsGoods() {
  if (!filteredData.value.length) {
    message("暂无可导出的积分商品", { type: "warning" });
    return;
  }
  const worksheet = utils.json_to_sheet(
    filteredData.value.map(item => ({
      积分商品: item.goodsName,
      商品编码: item.goodsCode,
      商品分类: item.categoryName,
      所需积分: item.points,
      库存: item.stock,
      兑换上限: item.exchangeLimit,
      适用店铺: item.storeName,
      状态: item.promotionStatusLabel
    }))
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "积分商品");
  writeFile(workbook, "积分商品.xlsx");
  message("积分商品导出成功", { type: "success" });
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
    keyword-label="积分商品"
    keyword-placeholder="请输入积分商品名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button :disabled="!filteredData.length" @click="exportPointsGoods">导出</el-button>
      <el-button
        type="danger"
        :disabled="!selectedIds.length"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
    </template>
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
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleSingleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="积分商品详情" size="42%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="积分商品">
        {{ detailRow.goodsName }}
      </el-descriptions-item>
      <el-descriptions-item label="商品编码">
        {{ detailRow.goodsCode }}
      </el-descriptions-item>
      <el-descriptions-item label="商品分类">
        {{ detailRow.categoryName }}
      </el-descriptions-item>
      <el-descriptions-item label="适用店铺">
        {{ detailRow.storeName }}
      </el-descriptions-item>
      <el-descriptions-item label="所需积分">
        {{ detailRow.points }}
      </el-descriptions-item>
      <el-descriptions-item label="库存">
        {{ detailRow.stock }}
      </el-descriptions-item>
      <el-descriptions-item label="兑换上限">
        {{ detailRow.exchangeLimit }}
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
