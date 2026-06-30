<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  getMemberPointsHistoryPage,
  getMemberPointsOverview,
  getMemberPointsStatistics
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const statistics = ref<Record<string, any>>({});
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const currentOverview = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });

const columns: TableColumnList = [
  { label: "会员名称", prop: "displayName", minWidth: 180 },
  { label: "积分类型", prop: "displayStatus", minWidth: 140 },
  { label: "积分变动", prop: "displayPoint", minWidth: 120 },
  { label: "变动时间", prop: "displayTime", minWidth: 180 },
  { label: "来源说明", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 120, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "流水总数", value: rows.value.length, accent: "orange" as const, hint: "当前积分流水记录" },
  { label: "增加流水", value: rows.value.filter(item => item.isIncrease).length, accent: "green" as const, hint: "积分增加记录" },
  { label: "减少流水", value: rows.value.filter(item => !item.isIncrease).length, accent: "blue" as const, hint: "积分减少记录" },
  { label: "总积分池", value: statistics.value.totalPoints || statistics.value.pointTotal || "-", accent: "purple" as const, hint: "后端积分统计汇总" }
]);

function normalizeRecord(item: Record<string, any>) {
  const typeText = String(item.type || item.pointType || item.pointTypeName || "-");
  const point = Number(item.point || item.points || item.changePoint || 0);
  return {
    ...item,
    id: item.id || item.memberId || `${item.memberId}-${item.createTime}`,
    isIncrease: point >= 0,
    displayName: item.memberName || item.nickname || "-",
    displayStatus: typeText,
    displayPoint: point,
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.reason || item.content || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 200
    };
    if (query.keyword) params.memberName = query.keyword;
    const res = await getMemberPointsHistoryPage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("积分记录加载失败，请稍后重试", { type: "error" });
  }
}

async function loadStatistics() {
  try {
    statistics.value = extractApiPayload<Record<string, any>>(await getMemberPointsStatistics()) || {};
  } catch (_error) {
    statistics.value = {};
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

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  currentOverview.value = null;
  if (!row.memberId && !row.id) {
    detailVisible.value = true;
    return;
  }
  try {
    currentOverview.value = extractApiPayload<Record<string, any>>(
      await getMemberPointsOverview(String(row.memberId || row.id))
    ) || null;
  } catch (_error) {
    currentOverview.value = null;
  }
  detailVisible.value = true;
}

function exportPointsHistory() {
  if (!rows.value.length) {
    message("暂无可导出的积分记录", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    会员名称: item.displayName,
    积分类型: item.displayStatus,
    积分变动: item.displayPoint,
    变动时间: item.displayTime,
    来源说明: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "积分记录");
  writeFile(workbook, "积分记录.xlsx");
  message("积分记录导出成功", { type: "success" });
}

onMounted(async () => {
  await Promise.all([loadData(), loadStatistics()]);
});
</script>

<template>
  <WholesaleAdminPage
    title="积分记录"
    description="承接会员积分流水、积分类型检索和单会员积分概览查看，作为会员积分台账页。"
    api-path="/manager/member/memberPointsHistory/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '统计汇总', value: '已承接', type: 'primary' },
      { label: '积分概览', value: '已接入', type: 'success' },
      { label: '流水导出', value: '已接入', type: 'warning' }
    ]"
    keyword-label="会员名称"
    keyword-placeholder="请输入会员名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportPointsHistory">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">概览</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="会员积分概览" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="会员名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="积分类型">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="积分变动">{{ currentRow.displayPoint }}</el-descriptions-item>
      <el-descriptions-item label="变动时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="来源说明">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="积分概览">
        <pre class="detail-json">{{ JSON.stringify(currentOverview || {}, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}
</style>
