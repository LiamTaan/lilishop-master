<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createArticleCategory,
  deleteArticleCategory,
  getArticleCategoryDetail,
  getArticleCategoryPage,
  updateArticleCategory
} from "@/api/super-admin";
import { extractApiPayload } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({
  name: "",
  sort: 0,
  level: 0,
  parentId: "",
  description: ""
});

const columns: TableColumnList = [
  { label: "分类名称", prop: "displayName", minWidth: 220 },
  { label: "层级", prop: "displayLevel", minWidth: 100 },
  { label: "排序值", prop: "displaySort", minWidth: 120 },
  { label: "说明", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "分类总数", value: rows.value.length, accent: "orange" as const, hint: "当前文章分类节点" },
  { label: "一级分类", value: rows.value.filter(item => Number(item.displayLevel) === 0).length, accent: "green" as const, hint: "顶级分类数量" },
  { label: "子级分类", value: rows.value.filter(item => Number(item.displayLevel) > 0).length, accent: "blue" as const, hint: "下级分类数量" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实分类接口" }
]);

function flattenTree(list: Record<string, any>[], bucket: Record<string, any>[] = []) {
  list.forEach(item => {
    bucket.push(item);
    const children = item.children || item.child || [];
    if (Array.isArray(children) && children.length) {
      flattenTree(children, bucket);
    }
  });
  return bucket;
}

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.categoryId || item.name,
    displayName: item.name || item.categoryName || "-",
    displayLevel: item.level ?? 0,
    displaySort: item.sort ?? 0,
    displayRemark: item.description || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getArticleCategoryPage();
    const payload = extractApiPayload<any>(res);
    const list = Array.isArray(payload) ? payload : [];
    let flat = flattenTree(list).map(normalizeRecord);
    if (query.keyword) {
      flat = flat.filter(item => item.displayName.includes(query.keyword));
    }
    rows.value = flat;
  } catch (_error) {
    message("文章分类加载失败，请稍后重试", { type: "error" });
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
  form.sort = 0;
  form.level = 0;
  form.parentId = "";
  form.description = "";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getArticleCategoryDetail(String(row.id));
    const detail = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
    form.name = detail.name || detail.displayName;
    form.sort = Number(detail.sort ?? detail.displaySort ?? 0);
    form.level = Number(detail.level ?? detail.displayLevel ?? 0);
    form.parentId = detail.parentId || "";
    form.description = detail.description || detail.displayRemark;
  } catch (_error) {
    form.name = row.name || row.displayName;
    form.sort = Number(row.sort ?? row.displaySort ?? 0);
    form.level = Number(row.level ?? row.displayLevel ?? 0);
    form.parentId = row.parentId || "";
    form.description = row.description || row.displayRemark;
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
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
    const payload = {
      name: form.name,
      sort: form.sort,
      level: form.level,
      parentId: form.parentId,
      description: form.description
    };
    if (editingRow.value) {
      await updateArticleCategory(String(editingRow.value.id), payload);
      message("文章分类修改成功", { type: "success" });
    } else {
      await createArticleCategory(payload);
      message("文章分类新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("文章分类保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除分类「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteArticleCategory(String(row.id));
    message("文章分类删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("文章分类删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="文章分类"
    description="承接文章分类树、分类层级维护和分类编辑删除动作，作为内容运营中的分类治理页。"
    api-path="/manager/other/articleCategory/all-children"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
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

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑文章分类' : '新增文章分类'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="分类名称" required><el-input v-model="form.name" placeholder="请输入分类名称" /></el-form-item>
      <el-form-item label="层级"><el-input-number v-model="form.level" :min="0" style="width: 100%" /></el-form-item>
      <el-form-item label="排序值"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
      <el-form-item label="上级ID"><el-input v-model="form.parentId" placeholder="请输入上级分类ID，可留空" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入分类说明" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="文章分类详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="分类名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="层级">{{ currentRow.displayLevel }}</el-descriptions-item>
      <el-descriptions-item label="排序值">{{ currentRow.displaySort }}</el-descriptions-item>
      <el-descriptions-item label="说明">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
