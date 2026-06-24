<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getAfterSalePage } from "@/api/order-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getAfterSaleStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "AfterSaleManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  applyTypeKeyword: ""
});

function normalizeAfterSaleRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.sn,
    afterSaleSn: item.afterSaleSn || item.sn || "-",
    orderSn: item.orderSn || "-",
    goodsName: item.goodsName || item.spuName || "-",
    applyType: item.applyType || item.afterSaleType || "-",
    status: item.status || item.serviceStatus || "-",
    applyAmount: item.applyAmount || item.actualRefundPrice || "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.applyTypeKeyword ? String(item.applyType).includes(extraFilters.applyTypeKeyword) : true
  )
);

const summaryCards = computed(() => [
  { label: "售后总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "待处理售后",
    value: filteredData.value.filter(item => String(item.status).toUpperCase().includes("WAIT")).length,
    accent: "blue" as const,
    hint: "待平台审核或处理"
  },
  {
    label: "退款金额",
    value: filteredData.value.reduce((sum, item) => sum + Number(item.applyAmount || 0), 0),
    accent: "green" as const,
    hint: "申请金额汇总"
  },
  { label: "售后治理", value: "已接入", accent: "purple" as const, hint: "售后流程页" }
]);

const detailItems = computed(() => [
  { label: "售后单号", value: detail.value.afterSaleSn || "-" },
  { label: "订单号", value: detail.value.orderSn || "-" },
  { label: "商品名称", value: detail.value.goodsName || "-" },
  { label: "售后类型", value: detail.value.applyType || "-" },
  { label: "售后状态", value: getAfterSaleStatusLabel(detail.value.status) },
  { label: "申请金额", value: `¥ ${detail.value.applyAmount || "-"}` }
]);

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10
  };
  if (query.keyword) params.sn = query.keyword;
  if (query.status) params.serviceStatus = query.status;
  const res = await getAfterSalePage(params);
  data.value = extractApiRecords(res).map(normalizeAfterSaleRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.applyTypeKeyword = "";
  loadData();
}

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="售后治理"
    description="承接平台售后查询、审核、收件地址与退款处理治理。"
    api-path="/manager/order/afterSale/page"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '售后状态', value: '已接入', type: 'warning' },
      { label: '售后类型', value: '前端补充', type: 'success' },
      { label: '退款金额', value: '已强化', type: 'primary' }
    ]"
    keyword-label="售后单/订单"
    keyword-placeholder="请输入售后单号或订单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="售后类型">
        <el-input
          v-model="extraFilters.applyTypeKeyword"
          placeholder="请输入售后类型"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="售后详情" size="40%">
    <el-descriptions :column="1" border>
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
