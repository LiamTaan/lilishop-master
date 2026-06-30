<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createLogistics,
  deleteLogistics,
  getLogisticsDetail,
  getLogisticsPage,
  updateLogistics
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });
const form = reactive({ name: "", code: "", standBy: "" });

const columns: TableColumnList = [
  { label: "物流公司", prop: "displayName", minWidth: 220 },
  { label: "公司编码", prop: "displayCode", minWidth: 180 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "备注", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "物流公司数", value: rows.value.length, accent: "orange" as const, hint: "当前物流基础资料" },
  { label: "编码完整", value: rows.value.filter(item => item.displayCode && item.displayCode !== "-").length, accent: "green" as const, hint: "已配置编码的物流公司" },
  { label: "待补备注", value: rows.value.filter(item => !item.displayRemark || item.displayRemark === "-").length, accent: "blue" as const, hint: "待完善备注信息" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实物流接口" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.logisticsId || item.name,
    displayName: item.name || item.logisticsName || "-",
    displayCode: item.code || "-",
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.standBy || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getLogisticsPage({ pageNumber: 1, pageSize: 200 });
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword) || String(item.displayCode).includes(query.keyword));
    rows.value = list;
    selectedRows.value = [];
  } catch (_error) {
    message("物流公司加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string }) {
  query.keyword = payload.keyword || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  editingRow.value = null;
  form.name = "";
  form.code = "";
  form.standBy = "";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getLogisticsDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.name = detail.name || detail.displayName;
    form.code = detail.code || detail.displayCode;
    form.standBy = detail.standBy || detail.displayRemark;
  } catch (_error) {
    form.name = row.name || row.displayName;
    form.code = row.code || row.displayCode;
    form.standBy = row.standBy || row.displayRemark;
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.name.trim()) {
    message("请输入物流公司名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { name: form.name, code: form.code, standBy: form.standBy };
    if (editingRow.value) {
      await updateLogistics(String(editingRow.value.id), payload);
      message("物流公司修改成功", { type: "success" });
    } else {
      await createLogistics(payload);
      message("物流公司新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("物流公司保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除物流公司「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteLogistics(String(row.id));
    message("物流公司删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("物流公司删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的物流公司", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 家物流公司吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteLogistics(id)));
    selectedRows.value = [];
    message("物流公司批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("物流公司批量删除失败", { type: "error" });
  }
}

function exportLogistics() {
  if (!rows.value.length) {
    message("暂无可导出的物流公司数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    物流公司: item.displayName,
    公司编码: item.displayCode,
    更新时间: item.displayTime,
    备注: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "物流公司");
  writeFile(workbook, "物流公司.xlsx");
  message("物流公司导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="物流公司"
    description="承接物流公司台账、公司编码维护和基础资料治理，作为基础支撑侧的真实配置页。"
    api-path="/manager/other/logistics/getByPage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '物流新增', value: '已接入', type: 'primary' },
      { label: '物流编辑', value: '已接入', type: 'success' },
      { label: '物流删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="公司名称/编码"
    keyword-placeholder="请输入物流公司名称或编码"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportLogistics">导出</el-button>
      <el-button type="primary" @click="openCreate">新增物流公司</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑物流公司' : '新增物流公司'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="公司名称" required><el-input v-model="form.name" placeholder="请输入物流公司名称" /></el-form-item>
      <el-form-item label="公司编码"><el-input v-model="form.code" placeholder="请输入物流公司编码" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.standBy" type="textarea" :rows="4" placeholder="请输入备注" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="物流公司详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="物流公司">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="公司编码">{{ currentRow.displayCode }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
