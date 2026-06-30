<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  getProcurementOrderDetail,
  getProcurementOrderPage
} from "@/api/procurement-governance";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getProcurementStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "ProcurementGovernanceOrder"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});

const summaryCards = computed(() => [
  {
    label: "采购单数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前已加载采购单"
  },
  {
    label: "采购金额",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.totalAmount || 0),
      0
    ),
    accent: "green" as const,
    hint: "采购总金额汇总"
  },
  {
    label: "采购数量",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.totalQuantity || 0),
      0
    ),
    accent: "blue" as const,
    hint: "采购商品数量汇总"
  },
  {
    label: "详情能力",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持详情抽屉"
  }
]);

const detailItems = computed(() => {
  const current = detail.value;
  if (!current) return [];
  return [
    { label: "采购单号", value: current.orderSn || "-" },
    { label: "店铺名称", value: current.storeName || "-" },
    { label: "制单人", value: current.makerName || "-" },
    { label: "采购状态", value: getProcurementStatusLabel(current.status) },
    { label: "采购金额", value: `¥ ${current.totalAmount ?? 0}` },
    { label: "采购数量", value: current.totalQuantity ?? 0 },
    { label: "审核时间", value: formatAdminDateTime(current.auditTime) },
    { label: "创建时间", value: formatAdminDateTime(current.createTime) },
    { label: "备注", value: current.remark || "-" }
  ];
});

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.orderSn,
    orderSn: item.orderSn || "-",
    storeName: item.storeName || "-",
    makerName: item.makerName || "-",
    totalAmount: Number(item.totalAmount || 0),
    totalQuantity: Number(item.totalQuantity || 0),
    status: item.status || "-",
    auditTime: item.auditTime,
    createTime: item.createTime,
    remark: item.remark || "-",
    items: Array.isArray(item.items) ? item.items : []
  };
}

async function loadData() {
  try {
    const res = await getProcurementOrderPage({
      pageNumber: 1,
      pageSize: 200,
      orderSn: query.keyword || undefined,
      status: query.status || undefined
    });
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("采购订单加载失败，请稍后重试", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getProcurementOrderDetail(String(row.id));
    detail.value = normalizeRow(
      extractApiPayload<Record<string, any>>(res) || row
    );
    detailVisible.value = true;
  } catch (_error) {
    message("采购订单详情加载失败", { type: "error" });
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

function exportOrders() {
  if (!rows.value.length) {
    message("暂无可导出的采购订单", { type: "warning" });
    return;
  }
  const worksheet = utils.json_to_sheet(
    rows.value.map(item => ({
      采购单号: item.orderSn,
      店铺名称: item.storeName,
      制单人: item.makerName,
      采购金额: item.totalAmount,
      采购数量: item.totalQuantity,
      采购状态: getProcurementStatusLabel(item.status),
      审核时间: formatAdminDateTime(item.auditTime),
      创建时间: formatAdminDateTime(item.createTime),
      备注: item.remark
    }))
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "采购订单");
  writeFile(workbook, "采购订单.xlsx");
  message("采购订单导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="采购订单管理"
    description="承接平台采购订单列表、状态筛选与详情抽屉，只对接管理端 /manager/procurement/order。"
    api-path="/manager/procurement/order"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待提交', value: 'DRAFT' },
      { label: '已提交', value: 'SUBMITTED' },
      { label: '待入库', value: 'PENDING_INBOUND' },
      { label: '部分入库', value: 'PARTIAL_INBOUND' },
      { label: '已完成', value: 'COMPLETED' },
      { label: '已关闭', value: 'CLOSED' },
      { label: '已拒绝', value: 'REJECTED' }
    ]"
    :quick-actions="[
      { label: '管理端路径', value: '已锁定', type: 'primary' },
      { label: '状态筛选', value: '已接入', type: 'success' },
      { label: '详情抽屉', value: '已接入', type: 'warning' }
    ]"
    keyword-label="采购单号"
    keyword-placeholder="请输入采购单号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button :disabled="!rows.length" @click="exportOrders">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="采购订单详情" size="48%">
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
      <h4 class="detail-block__title">采购明细</h4>
      <el-table :data="detail?.items || []" border>
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="skuId" label="SKU ID" min-width="180" />
        <el-table-column prop="quantity" label="采购数量" min-width="100" />
        <el-table-column prop="retailPrice" label="零售价" min-width="100" />
        <el-table-column
          prop="unitPriceWithTax"
          label="含税单价"
          min-width="110"
        />
        <el-table-column
          prop="subtotalWithTax"
          label="含税小计"
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
