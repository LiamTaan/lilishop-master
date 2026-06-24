<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createShortcutNav,
  deleteShortcutNav,
  getShortcutNavDetail,
  getShortcutNavPage,
  updateShortcutNav
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });
const form = reactive({ name: "", url: "", sort: 0, enable: true });

const columns: TableColumnList = [
  { label: "分类名称", prop: "displayName", minWidth: 220 },
  { label: "启用状态", prop: "displayStatus", minWidth: 120 },
  { label: "跳转地址", prop: "displayRemark", minWidth: 240, showOverflowTooltip: true },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "分类总数", value: rows.value.length, accent: "orange" as const, hint: "当前中间分类配置" },
  { label: "已启用", value: rows.value.filter(item => item.displayStatus === "启用").length, accent: "green" as const, hint: "正在展示的分类" },
  { label: "未启用", value: rows.value.filter(item => item.displayStatus !== "启用").length, accent: "blue" as const, hint: "待上线或停用分类" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接首页分类接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.navId || item.name,
    displayName: item.name || item.title || "-",
    displayStatus: item.enable === true || item.status === true || item.status === "OPEN" ? "启用" : "停用",
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.url || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getShortcutNavPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("中间分类加载失败，请稍后重试", { type: "error" });
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

function openCreate() {
  editingRow.value = null;
  form.name = "";
  form.url = "";
  form.sort = 0;
  form.enable = true;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getShortcutNavDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.name = detail.name || detail.displayName;
    form.url = detail.url || detail.displayRemark;
    form.sort = Number(detail.sort || 0);
    form.enable = detail.enable ?? detail.displayStatus === "启用";
  } catch (_error) {
    form.name = row.name || row.displayName;
    form.url = row.url || row.displayRemark;
    form.sort = Number(row.sort || 0);
    form.enable = row.enable ?? row.displayStatus === "启用";
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.name.trim()) {
    message("请输入分类名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { name: form.name, url: form.url, sort: form.sort, enable: form.enable };
    if (editingRow.value) {
      await updateShortcutNav(String(editingRow.value.id), payload);
      message("中间分类修改成功", { type: "success" });
    } else {
      await createShortcutNav(payload);
      message("中间分类新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("中间分类保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除分类「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteShortcutNav(String(row.id));
    message("中间分类删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("中间分类删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="中间分类"
    description="承接首页中间分类配置、分类入口维护和启停治理，作为内容运营中的分类配置页。"
    api-path="/manager/other/shortcutNav"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[{ label: '启用', value: '启用' }, { label: '停用', value: '停用' }]"
    :quick-actions="[
      { label: '分类新增', value: '已接入', type: 'primary' },
      { label: '分类编辑', value: '已接入', type: 'success' },
      { label: '分类删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="分类名称"
    keyword-placeholder="请输入分类名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑中间分类' : '新增中间分类'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="分类名称" required><el-input v-model="form.name" placeholder="请输入分类名称" /></el-form-item>
      <el-form-item label="跳转地址"><el-input v-model="form.url" placeholder="请输入跳转地址" /></el-form-item>
      <el-form-item label="排序值"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
      <el-form-item label="启用状态"><el-switch v-model="form.enable" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="中间分类详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="分类名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="启用状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="跳转地址">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
