<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  downloadInventoryCountItems,
  getInventoryCountDetail,
  getInventoryCountItemsPage,
  getInventoryCountPage
} from "@/api/procurement-governance";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "ProcurementGovernanceInventoryCount"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any> | null>(null);
const detailRows = ref<Record<string, any>[]>([]);

const summaryCards = computed(() => [
  {
    label: "盘点单数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前已加载盘点单"
  },
  {
    label: "盘点商品数",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.itemTotal || 0),
      0
    ),
    accent: "green" as const,
    hint: "盘点商品总数汇总"
  },
  {
    label: "明细分页",
    value: "已接入",
    accent: "blue" as const,
    hint: "支持明细分页接口"
  },
  {
    label: "下载入口",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持下载盘点明细"
  }
]);

const detailItems = computed(() => {
  const current = detail.value;
  if (!current) return [];
  return [
    { label: "盘点单号", value: current.sn || "-" },
    { label: "店铺ID", value: current.storeId || "-" },
    { label: "制单人", value: current.makerName || "-" },
    { label: "商品总数", value: current.itemTotal ?? 0 },
    { label: "盘点时间", value: formatAdminDateTime(current.countTime) },
    { label: "创建时间", value: formatAdminDateTime(current.createTime) }
  ];
});

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.sn,
    sn: item.sn || "-",
    storeId: item.storeId || "-",
    makerName: item.makerName || "-",
    itemTotal: Number(item.itemTotal || 0),
    countTime: item.countTime,
    createTime: item.createTime
  };
}

async function loadData() {
  try {
    const res = await getInventoryCountPage({ pageNumber: 1, pageSize: 200 });
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("盘点管理加载失败，请稍后重试", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const [detailRes, itemsRes] = await Promise.all([
      getInventoryCountDetail(String(row.id)),
      getInventoryCountItemsPage(String(row.id), {
        pageNumber: 1,
        pageSize: 200
      })
    ]);
    detail.value = normalizeRow(
      extractApiPayload<Record<string, any>>(detailRes) || row
    );
    detailRows.value = extractApiRecords(itemsRes);
    detailVisible.value = true;
  } catch (_error) {
    message("盘点详情加载失败", { type: "error" });
  }
}

async function handleDownload(row: Record<string, any>) {
  try {
    const res = await downloadInventoryCountItems(String(row.id));
    const list = extractApiRecords(res);
    const blob = new Blob([JSON.stringify(list, null, 2)], {
      type: "application/json;charset=utf-8"
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${row.sn || "inventory-count"}-items.json`;
    link.click();
    URL.revokeObjectURL(url);
    message("盘点明细下载成功", { type: "success" });
  } catch (_error) {
    message("盘点明细下载失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="盘点管理"
    description="承接管理端盘点列表、详情、明细分页与下载入口，只对接 /manager/procurement/inventory-count。"
    api-path="/manager/procurement/inventory-count/page"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '详情查看', value: '已接入', type: 'primary' },
      { label: '明细分页', value: '已接入', type: 'success' },
      { label: '下载入口', value: '已接入', type: 'warning' }
    ]"
    keyword-label="说明"
    keyword-placeholder="盘点管理当前不支持关键词筛选"
    @search="loadData"
    @reset="loadData"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="handleDownload(row)"
        >下载</el-button
      >
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="盘点详情" size="54%">
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
      <h4 class="detail-block__title">盘点明细</h4>
      <el-table :data="detailRows" border>
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="skuName" label="规格" min-width="140" />
        <el-table-column prop="marketEnable" label="上架状态" min-width="100" />
        <el-table-column prop="quantity" label="库存数量" min-width="100" />
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
