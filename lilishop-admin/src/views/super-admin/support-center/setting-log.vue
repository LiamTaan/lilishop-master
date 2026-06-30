<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  deleteAllSettingLog,
  deleteSettingLog,
  getSettingLogPage
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", operatorName: "", type: "" });

const columns: TableColumnList = [
  { label: "操作人", prop: "displayName", minWidth: 160 },
  { label: "日志类型", prop: "displayStatus", minWidth: 140 },
  { label: "关键字", prop: "displayKeyword", minWidth: 180 },
  { label: "操作时间", prop: "displayTime", minWidth: 180 },
  { label: "操作内容", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 160, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "日志总数", value: rows.value.length, accent: "orange" as const, hint: "当前配置日志台账" },
  { label: "有操作人", value: rows.value.filter(item => item.displayName !== "-").length, accent: "green" as const, hint: "可追溯到操作人" },
  { label: "有关键字", value: rows.value.filter(item => item.displayKeyword !== "-").length, accent: "blue" as const, hint: "可按关键字检索" },
  { label: "治理动作", value: "查看/删除/清空", accent: "purple" as const, hint: "承接真实日志接口" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.logId || item.createTime,
    displayName: item.operatorName || item.username || "-",
    displayStatus: item.type || item.logType || "-",
    displayKeyword: item.searchKey || item.keyword || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.content || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params = {
      pageNumber: 1,
      pageSize: 200,
      searchKey: query.keyword || "all",
      operatorName: query.operatorName || undefined,
      type: query.type ? Number(query.type) : undefined
    };
    const res = await getSettingLogPage(params);
    rows.value = extractApiRecords(res).map(normalizeRecord);
    selectedRows.value = [];
  } catch (_error) {
    message("配置日志加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  query.type = payload.status || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.operatorName = "";
  query.type = "";
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除这条配置日志吗？`, "删除确认", { type: "warning" });
  try {
    await deleteSettingLog([String(row.id)]);
    message("配置日志删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("配置日志删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的配置日志", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 条配置日志吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deleteSettingLog(selectedIds.value);
    selectedRows.value = [];
    message("配置日志批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("配置日志批量删除失败", { type: "error" });
  }
}

async function handleDeleteAll() {
  await ElMessageBox.confirm("确认清空全部配置日志吗？", "清空确认", { type: "warning" });
  try {
    await deleteAllSettingLog();
    message("配置日志已全部清空", { type: "success" });
    await loadData();
  } catch (_error) {
    message("配置日志清空失败", { type: "error" });
  }
}

function exportSettingLogs() {
  if (!rows.value.length) {
    message("暂无可导出的配置日志", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    操作人: item.displayName,
    日志类型: item.displayStatus,
    关键字: item.displayKeyword,
    操作时间: item.displayTime,
    操作内容: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "系统维护记录");
  writeFile(workbook, "系统维护记录.xlsx");
  message("配置日志导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="系统维护记录"
    description="承接平台配置变更日志、操作详情查看和日志清理动作，作为系统治理的日志台账页。"
    api-path="/manager/setting/log/getAllByPage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '类型 0', value: '0' },
      { label: '类型 1', value: '1' },
      { label: '类型 2', value: '2' }
    ]"
    :quick-actions="[
      { label: '日志查看', value: '已接入', type: 'primary' },
      { label: '单条删除', value: '已接入', type: 'warning' },
      { label: '全部清空', value: '已接入', type: 'success' }
    ]"
    keyword-label="关键字"
    keyword-placeholder="请输入搜索关键字"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #filters-extra>
      <el-form-item label="操作人">
        <el-input v-model="query.operatorName" placeholder="请输入操作人" clearable />
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportSettingLogs">导出</el-button>
      <el-button type="danger" plain @click="handleDeleteAll">清空日志</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="配置日志详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="操作人">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="日志类型">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="关键字">{{ currentRow.displayKeyword }}</el-descriptions-item>
      <el-descriptions-item label="操作时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="操作内容">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
