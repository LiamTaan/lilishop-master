<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  getProcurementInboundDetail,
  getProcurementInboundItems,
  getProcurementInboundPage
} from "@/api/procurement-governance";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "ProcurementGovernanceInbound"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  procurementOrderId: ""
});

const summaryCards = computed(() => [
  {
    label: "入库单数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前已加载入库单"
  },
  {
    label: "已确认入库量",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.confirmedQuantity || 0),
      0
    ),
    accent: "green" as const,
    hint: "累计已确认入库数量"
  },
  {
    label: "待确认入库量",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.pendingQuantity || 0),
      0
    ),
    accent: "blue" as const,
    hint: "累计待确认入库数量"
  },
  {
    label: "明细抽屉",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持入库明细查看"
  }
]);

const detailItems = computed(() => {
  const current = detail.value;
  if (!current) return [];
  return [
    { label: "入库单号", value: current.inboundSn || "-" },
    { label: "采购单ID", value: current.procurementOrderId || "-" },
    { label: "店铺ID", value: current.storeId || "-" },
    { label: "制单人", value: current.operatorName || "-" },
    { label: "预计入库量", value: current.expectedQuantity ?? 0 },
    { label: "已确认入库量", value: current.confirmedQuantity ?? 0 },
    { label: "待确认入库量", value: current.pendingQuantity ?? 0 },
    { label: "入库成本", value: `¥ ${current.totalCost ?? 0}` },
    { label: "零售金额", value: `¥ ${current.totalRetailAmount ?? 0}` },
    { label: "入库时间", value: formatAdminDateTime(current.inboundTime) }
  ];
});

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.inboundSn,
    inboundSn: item.inboundSn || "-",
    procurementOrderId: item.procurementOrderId || "-",
    storeId: item.storeId || "-",
    operatorName: item.operatorName || "-",
    expectedQuantity: Number(item.expectedQuantity || 0),
    confirmedQuantity: Number(item.confirmedQuantity || 0),
    pendingQuantity: Number(item.pendingQuantity || 0),
    totalCost: Number(item.totalCost || 0),
    totalRetailAmount: Number(item.totalRetailAmount || 0),
    inboundTime: item.inboundTime,
    items: Array.isArray(item.items) ? item.items : []
  };
}

async function loadData() {
  try {
    const res = await getProcurementInboundPage({
      pageNumber: 1,
      pageSize: 200,
      inboundSn: query.keyword || undefined,
      procurementOrderId: query.procurementOrderId || undefined
    });
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("采购入库列表加载失败，请稍后重试", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const [detailRes, itemsRes] = await Promise.all([
      getProcurementInboundDetail(String(row.id)),
      getProcurementInboundItems(String(row.id))
    ]);
    const current = normalizeRow(
      extractApiPayload<Record<string, any>>(detailRes) || row
    );
    current.items = extractApiRecords(itemsRes);
    detail.value = current;
    detailVisible.value = true;
  } catch (_error) {
    message("采购入库详情加载失败", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string }) {
  query.keyword = payload.keyword || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.procurementOrderId = "";
  loadData();
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="采购入库管理"
    description="承接管理端采购入库列表、详情与入库明细抽屉，只对接 /manager/procurement/inbound。"
    api-path="/manager/procurement/inbound"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '管理端路径', value: '已锁定', type: 'primary' },
      { label: '详情抽屉', value: '已接入', type: 'success' },
      { label: '明细列表', value: '已接入', type: 'warning' }
    ]"
    keyword-label="入库单号"
    keyword-placeholder="请输入入库单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="采购单ID">
        <el-input
          v-model="query.procurementOrderId"
          placeholder="请输入采购单ID"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="采购入库详情" size="52%">
    <el-descriptions v-if="detail" :column="1" border>
      <el-descriptions-item
        v-for="item in detailItems"
        :key="item.label"
        :label="item.label"
      >
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="detail-block">
      <h4 class="detail-block__title">入库明细</h4>
      <el-table :data="detail?.items || []" border>
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="skuId" label="SKU ID" min-width="180" />
        <el-table-column
          prop="expectedQuantity"
          label="预计量"
          min-width="100"
        />
        <el-table-column
          prop="inboundQuantity"
          label="实际入库量"
          min-width="110"
        />
      </el-table>
    </div>
  </el-drawer>
</template>

<style scoped>
.detail-block {
  margin-top: 20px;
}

.detail-block__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: #2f3135;
}
</style>
