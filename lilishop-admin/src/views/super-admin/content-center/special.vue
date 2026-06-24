<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createSpecial,
  deleteSpecial,
  getSpecialDetail,
  getSpecialPage,
  updateSpecial
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({ name: "", description: "", sort: 0 });

const columns: TableColumnList = [
  { label: "专题名称", prop: "displayName", minWidth: 220 },
  { label: "排序值", prop: "displaySort", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "说明", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "专题总数", value: rows.value.length, accent: "orange" as const, hint: "当前专题活动台账" },
  { label: "高优先级", value: rows.value.filter(item => Number(item.displaySort) <= 10).length, accent: "green" as const, hint: "排序靠前专题" },
  { label: "待补说明", value: rows.value.filter(item => !item.displayRemark || item.displayRemark === "-").length, accent: "blue" as const, hint: "待完善专题说明" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实专题接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.specialId || item.name,
    displayName: item.name || item.specialName || "-",
    displaySort: item.sort ?? 0,
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.remark || item.description || "-"
  };
}

async function loadData() {
  try {
    const res = await getSpecialPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    rows.value = list;
  } catch (_error) {
    message("专题管理加载失败，请稍后重试", { type: "error" });
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

function openCreate() {
  editingRow.value = null;
  form.name = "";
  form.description = "";
  form.sort = 0;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getSpecialDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.name = detail.name || detail.displayName;
    form.description = detail.description || detail.displayRemark;
    form.sort = Number(detail.sort ?? detail.displaySort ?? 0);
  } catch (_error) {
    form.name = row.name || row.displayName;
    form.description = row.description || row.displayRemark;
    form.sort = Number(row.sort ?? row.displaySort ?? 0);
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.name.trim()) {
    message("请输入专题名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { name: form.name, description: form.description, sort: form.sort };
    if (editingRow.value) {
      await updateSpecial(String(editingRow.value.id), payload);
      message("专题修改成功", { type: "success" });
    } else {
      await createSpecial(payload);
      message("专题新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("专题保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除专题「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteSpecial(String(row.id));
    message("专题删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("专题删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="专题管理"
    description="承接专题活动台账、专题详情和新增编辑删除动作，作为内容运营里的专题治理页。"
    api-path="/manager/other/special"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '专题新增', value: '已接入', type: 'primary' },
      { label: '专题编辑', value: '已接入', type: 'success' },
      { label: '专题删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="专题名称"
    keyword-placeholder="请输入专题名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增专题</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑专题' : '新增专题'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="专题名称" required><el-input v-model="form.name" placeholder="请输入专题名称" /></el-form-item>
      <el-form-item label="排序值"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
      <el-form-item label="专题说明"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入专题说明" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="专题详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="专题名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="排序值">{{ currentRow.displaySort }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="说明">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
