<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createAfterSaleReason,
  deleteAfterSaleReason,
  getAfterSaleReasonDetail,
  getAfterSaleReasonPage,
  updateAfterSaleReason
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
const query = reactive({ keyword: "", status: "", serviceType: "RETURN_GOODS" });
const form = reactive({ reason: "", serviceType: "RETURN_GOODS" });

const columns: TableColumnList = [
  { label: "售后原因", prop: "displayName", minWidth: 220 },
  { label: "售后类型", prop: "displayType", minWidth: 140 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "备注", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "原因总数", value: rows.value.length, accent: "orange" as const, hint: "当前售后原因字典" },
  { label: "退货退款", value: rows.value.filter(item => item.displayType === "退货退款").length, accent: "green" as const, hint: "当前退货退款原因" },
  { label: "仅退款/换货", value: rows.value.filter(item => item.displayType !== "退货退款").length, accent: "blue" as const, hint: "其他售后原因" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实售后原因接口" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function typeLabel(type: string) {
  if (type === "RETURN_GOODS") return "退货退款";
  if (type === "RETURN_MONEY") return "仅退款";
  if (type === "CHANGE_GOODS") return "换货";
  return type || "-";
}

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.reasonId || item.reason,
    displayName: item.reason || item.name || "-",
    displayType: typeLabel(item.serviceType),
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.remark || item.description || "-"
  };
}

async function loadData() {
  try {
    const res = await getAfterSaleReasonPage({
      serviceType: query.serviceType,
      pageNumber: 1,
      pageSize: 200
    });
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    rows.value = list;
    selectedRows.value = [];
  } catch (_error) {
    message("售后原因加载失败，请稍后重试", { type: "error" });
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
  form.reason = "";
  form.serviceType = query.serviceType;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getAfterSaleReasonDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.reason = detail.reason || detail.displayName;
    form.serviceType = detail.serviceType || query.serviceType;
  } catch (_error) {
    form.reason = row.reason || row.displayName;
    form.serviceType = row.serviceType || query.serviceType;
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.reason.trim()) {
    message("请输入售后原因", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { reason: form.reason, serviceType: form.serviceType };
    if (editingRow.value) {
      await updateAfterSaleReason(String(editingRow.value.id), payload);
      message("售后原因修改成功", { type: "success" });
    } else {
      await createAfterSaleReason(payload);
      message("售后原因新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("售后原因保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除售后原因「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteAfterSaleReason(String(row.id));
    message("售后原因删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("售后原因删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的售后原因", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 条售后原因吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteAfterSaleReason(id)));
    selectedRows.value = [];
    message("售后原因批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("售后原因批量删除失败", { type: "error" });
  }
}

function exportReasons() {
  if (!rows.value.length) {
    message("暂无可导出的售后原因数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    售后原因: item.displayName,
    售后类型: item.displayType,
    更新时间: item.displayTime,
    备注: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "售后原因");
  writeFile(workbook, "售后原因.xlsx");
  message("售后原因导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="售后原因"
    description="承接售后原因字典、按售后类型治理以及新增编辑删除动作，作为基础支撑的真实配置页。"
    api-path="/manager/order/afterSaleReason/getByPage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '原因新增', value: '已接入', type: 'primary' },
      { label: '原因编辑', value: '已接入', type: 'success' },
      { label: '原因删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="原因名称"
    keyword-placeholder="请输入售后原因"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #filters-extra>
      <el-form-item label="售后类型">
        <el-select v-model="query.serviceType" style="width: 180px" @change="loadData">
          <el-option label="退货退款" value="RETURN_GOODS" />
          <el-option label="仅退款" value="RETURN_MONEY" />
          <el-option label="换货" value="CHANGE_GOODS" />
        </el-select>
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportReasons">导出</el-button>
      <el-button type="primary" @click="openCreate">新增售后原因</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑售后原因' : '新增售后原因'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="售后原因" required><el-input v-model="form.reason" placeholder="请输入售后原因" /></el-form-item>
      <el-form-item label="售后类型">
        <el-select v-model="form.serviceType" style="width: 100%">
          <el-option label="退货退款" value="RETURN_GOODS" />
          <el-option label="仅退款" value="RETURN_MONEY" />
          <el-option label="换货" value="CHANGE_GOODS" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="售后原因详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="售后原因">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="售后类型">{{ currentRow.displayType }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
