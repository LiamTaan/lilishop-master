<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createCustomWord,
  deleteCustomWord,
  getCustomWordsPage,
  updateCustomWord
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "CustomWordsManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({
  id: "",
  words: "",
  analysis: "",
  nature: ""
});

const columns: TableColumnList = [
  { label: "词条", prop: "displayName", minWidth: 220 },
  { label: "分词器", prop: "displayAnalysis", minWidth: 160 },
  { label: "词性", prop: "displayNature", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "词条总数", value: rows.value.length, accent: "orange" as const, hint: "当前自定义词库台账" },
  { label: "已配分词器", value: rows.value.filter(item => item.displayAnalysis !== "-").length, accent: "green" as const, hint: "已绑定分词器的词条" },
  { label: "已配词性", value: rows.value.filter(item => item.displayNature !== "-").length, accent: "blue" as const, hint: "已配置词性的词条" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实词库接口" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.words || item.word,
    displayName: item.words || item.word || item.keyword || "-",
    displayAnalysis: item.analysis || item.analyzer || "-",
    displayNature: item.nature || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.remark || item.description || "-"
  };
}

async function loadData() {
  try {
    const words = query.keyword || "*";
    const res = await getCustomWordsPage({ words });
    rows.value = extractApiRecords(res).map(normalizeRecord);
  } catch (_error) {
    message("自定义词库加载失败，请稍后重试", { type: "error" });
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
  form.id = "";
  form.words = "";
  form.analysis = "";
  form.nature = "";
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.id = String(row.id || "");
  form.words = row.words || row.word || row.displayName || "";
  form.analysis = row.analysis || row.displayAnalysis || "";
  form.nature = row.nature || row.displayNature || "";
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.words.trim()) {
    message("请输入词条", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      id: form.id || undefined,
      words: form.words,
      analysis: form.analysis,
      nature: form.nature
    };
    if (editingRow.value) {
      await updateCustomWord(payload);
      message("自定义词条修改成功", { type: "success" });
    } else {
      await createCustomWord(payload);
      message("自定义词条新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("自定义词条保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除词条「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteCustomWord(String(row.id));
    message("自定义词条删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("自定义词条删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的词条", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个词条吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteCustomWord(id)));
    selectedRows.value = [];
    message("自定义词条批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("自定义词条批量删除失败", { type: "error" });
  }
}

function exportCustomWords() {
  if (!rows.value.length) {
    message("暂无可导出的自定义词条数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    词条: item.displayName,
    分词器: item.displayAnalysis,
    词性: item.displayNature,
    更新时间: item.displayTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "自定义词库");
  writeFile(workbook, "自定义词库.xlsx");
  message("自定义词条导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="自定义词库"
    description="承接自定义词条台账、分词器维护、词性维护和增改删动作，作为内容运营的词库治理页。"
    api-path="/manager/other/customWords/page"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '词条新增', value: '已接入', type: 'primary' },
      { label: '词条编辑', value: '已接入', type: 'success' },
      { label: '词条删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="词条"
    keyword-placeholder="请输入词条"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportCustomWords">导出</el-button>
      <el-button type="primary" @click="openCreate">新增词条</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑自定义词条' : '新增自定义词条'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="词条" required>
        <el-input v-model="form.words" placeholder="请输入词条" />
      </el-form-item>
      <el-form-item label="分词器">
        <el-input v-model="form.analysis" placeholder="请输入分词器" />
      </el-form-item>
      <el-form-item label="词性">
        <el-input v-model="form.nature" placeholder="请输入词性" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="词条详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="词条">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="分词器">{{ currentRow.displayAnalysis }}</el-descriptions-item>
      <el-descriptions-item label="词性">{{ currentRow.displayNature }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
