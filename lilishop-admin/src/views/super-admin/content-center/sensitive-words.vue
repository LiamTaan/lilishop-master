<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createSensitiveWord,
  deleteSensitiveWord,
  getSensitiveWordDetail,
  getSensitiveWordsPage,
  updateSensitiveWord
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
const form = reactive({ sensitiveWord: "", category: "", remark: "" });

const columns: TableColumnList = [
  { label: "敏感词", prop: "displayName", minWidth: 220 },
  { label: "分类", prop: "displayCategory", minWidth: 140 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "备注", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "敏感词总数", value: rows.value.length, accent: "orange" as const, hint: "当前敏感词台账" },
  { label: "有分类", value: rows.value.filter(item => item.displayCategory !== "-").length, accent: "green" as const, hint: "已配置词类" },
  { label: "待补备注", value: rows.value.filter(item => !item.displayRemark || item.displayRemark === "-").length, accent: "blue" as const, hint: "待完善词条说明" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实敏感词接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.sensitiveWordId || item.sensitiveWord || item.keyword,
    displayName: item.sensitiveWord || item.keyword || item.name || "-",
    displayCategory: item.category || item.type || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.remark || item.description || "-"
  };
}

async function loadData() {
  try {
    const res = await getSensitiveWordsPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    rows.value = list;
  } catch (_error) {
    message("敏感词加载失败，请稍后重试", { type: "error" });
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
  form.sensitiveWord = "";
  form.category = "";
  form.remark = "";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getSensitiveWordDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.sensitiveWord = detail.sensitiveWord || detail.displayName;
    form.category = detail.category || detail.displayCategory;
    form.remark = detail.remark || detail.displayRemark;
  } catch (_error) {
    form.sensitiveWord = row.sensitiveWord || row.displayName;
    form.category = row.category || row.displayCategory;
    form.remark = row.remark || row.displayRemark;
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.sensitiveWord.trim()) {
    message("请输入敏感词", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { sensitiveWord: form.sensitiveWord, category: form.category, remark: form.remark };
    if (editingRow.value) {
      await updateSensitiveWord(String(editingRow.value.id), payload);
      message("敏感词修改成功", { type: "success" });
    } else {
      await createSensitiveWord(payload);
      message("敏感词新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("敏感词保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除敏感词「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteSensitiveWord([String(row.id)]);
    message("敏感词删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("敏感词删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="敏感词管理"
    description="承接敏感词台账、词条详情和敏感词新增编辑删除动作，作为内容运营中的词库治理页。"
    api-path="/manager/other/sensitiveWords"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '词条新增', value: '已接入', type: 'primary' },
      { label: '词条编辑', value: '已接入', type: 'success' },
      { label: '词条删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="敏感词"
    keyword-placeholder="请输入敏感词"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增敏感词</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑敏感词' : '新增敏感词'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="敏感词" required><el-input v-model="form.sensitiveWord" placeholder="请输入敏感词" /></el-form-item>
      <el-form-item label="分类"><el-input v-model="form.category" placeholder="请输入分类" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入备注" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="敏感词详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="敏感词">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="分类">{{ currentRow.displayCategory }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
