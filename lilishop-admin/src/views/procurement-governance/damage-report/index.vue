<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  getDamageReportDetail,
  getDamageReportItems,
  getDamageReportPage
} from "@/api/procurement-governance";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns, getDamageStatusLabel } from "./columns";

defineOptions({
  name: "ProcurementGovernanceDamageReport"
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
    label: "报损单数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前已加载报损单"
  },
  {
    label: "报损数量",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.totalQuantity || 0),
      0
    ),
    accent: "green" as const,
    hint: "累计报损数量"
  },
  {
    label: "报损金额",
    value: rows.value.reduce(
      (sum, item) => sum + Number(item.totalAmount || 0),
      0
    ),
    accent: "blue" as const,
    hint: "累计报损金额"
  },
  {
    label: "详情能力",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持报损明细查看"
  }
]);

const detailItems = computed(() => {
  const current = detail.value;
  if (!current) return [];
  return [
    { label: "报损单号", value: current.sn || "-" },
    { label: "店铺ID", value: current.storeId || "-" },
    { label: "状态", value: getDamageStatusLabel(current.status) },
    { label: "报损日期", value: formatAdminDateTime(current.damageDate) },
    { label: "报损原因ID", value: current.damageReasonId || "-" },
    { label: "报损数量", value: current.totalQuantity ?? 0 },
    { label: "报损金额", value: `¥ ${current.totalAmount ?? 0}` },
    { label: "备注", value: current.remark || "-" },
    { label: "凭证图片", value: current.evidence || "-" }
  ];
});

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.sn,
    sn: item.sn || "-",
    storeId: item.storeId || "-",
    status: item.status || "-",
    damageDate: item.damageDate,
    damageReasonId: item.damageReasonId || "-",
    remark: item.remark || "-",
    evidence: item.evidence || "-",
    totalQuantity: Number(item.totalQuantity || 0),
    totalAmount: Number(item.totalAmount || 0)
  };
}

async function loadData() {
  try {
    const res = await getDamageReportPage({
      pageNumber: 1,
      pageSize: 200,
      storeId: query.keyword || undefined,
      status: query.status || undefined
    });
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("报损管理加载失败，请稍后重试", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const [detailRes, itemsRes] = await Promise.all([
      getDamageReportDetail(String(row.id)),
      getDamageReportItems(String(row.id))
    ]);
    detail.value = normalizeRow(
      extractApiPayload<Record<string, any>>(detailRes) || row
    );
    detail.value.items = extractApiRecords(itemsRes);
    detailVisible.value = true;
  } catch (_error) {
    message("报损详情加载失败", { type: "error" });
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

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="报损管理"
    description="承接管理端报损列表、状态筛选与报损明细查看，只对接 /manager/procurement/damage-report。"
    api-path="/manager/procurement/damage-report/page"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '草稿', value: 'DRAFT' },
      { label: '已提交', value: 'SUBMITTED' },
      { label: '已通过', value: 'APPROVED' },
      { label: '已驳回', value: 'REJECTED' },
      { label: '已取消', value: 'CANCELLED' },
      { label: '已完成', value: 'COMPLETED' }
    ]"
    :quick-actions="[
      { label: '状态筛选', value: '已接入', type: 'primary' },
      { label: '详情查看', value: '已接入', type: 'success' },
      { label: '报损明细', value: '已接入', type: 'warning' }
    ]"
    keyword-label="店铺ID"
    keyword-placeholder="请输入店铺ID"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="报损详情" size="54%">
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
      <h4 class="detail-block__title">报损明细</h4>
      <el-table :data="detail?.items || []" border>
        <el-table-column prop="goodsId" label="商品ID" min-width="180" />
        <el-table-column prop="skuId" label="SKU ID" min-width="180" />
        <el-table-column prop="quantity" label="报损数量" min-width="100" />
        <el-table-column prop="unitPrice" label="单价" min-width="100" />
        <el-table-column prop="amount" label="金额" min-width="100" />
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
