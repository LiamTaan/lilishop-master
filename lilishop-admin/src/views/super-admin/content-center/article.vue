<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createArticle,
  deleteArticle,
  getArticleDetail,
  getArticlePage,
  updateArticle,
  updateArticleStatus
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
const form = reactive({
  title: "",
  content: "",
  categoryName: "",
  sort: 0,
  status: true
});

const columns: TableColumnList = [
  { label: "文章标题", prop: "displayName", minWidth: 240 },
  { label: "发布状态", prop: "displayStatus", minWidth: 120 },
  { label: "文章分类", prop: "displayCategory", minWidth: 160 },
  { label: "发布时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 260, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "文章总数", value: rows.value.length, accent: "orange" as const, hint: "当前文章台账" },
  { label: "已发布", value: rows.value.filter(item => item.isPublished).length, accent: "green" as const, hint: "线上可见文章" },
  { label: "未发布", value: rows.value.filter(item => !item.isPublished).length, accent: "blue" as const, hint: "草稿或下线文章" },
  { label: "治理动作", value: "新增/编辑/上下线/删除", accent: "purple" as const, hint: "承接真实文章接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  const statusValue = item.status ?? item.enable ?? item.articleStatus;
  const isPublished =
    statusValue === true ||
    statusValue === 1 ||
    String(statusValue).toLowerCase() === "true" ||
    String(statusValue).toUpperCase() === "UP";
  return {
    ...item,
    id: item.id || item.articleId || item.title,
    isPublished,
    displayName: item.title || item.articleTitle || "-",
    displayStatus: isPublished ? "已发布" : "未发布",
    displayCategory: item.categoryName || item.articleCategoryName || "-",
    displayTime: item.createTime || item.publishTime || item.updateTime || "-",
    displayRemark: item.content || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getArticlePage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(item => item.displayName.includes(query.keyword));
    }
    if (query.status) {
      list = list.filter(item => item.displayStatus === query.status);
    }
    rows.value = list;
  } catch (_error) {
    message("文章管理加载失败，请稍后重试", { type: "error" });
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
  form.title = "";
  form.content = "";
  form.categoryName = "";
  form.sort = 0;
  form.status = true;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getArticleDetail(String(row.id));
    const detail = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
    form.title = detail.title || detail.displayName;
    form.content = detail.content || detail.displayRemark;
    form.categoryName = detail.categoryName || detail.displayCategory;
    form.sort = Number(detail.sort || 0);
    form.status = detail.status ?? detail.isPublished;
  } catch (_error) {
    form.title = row.title || row.displayName;
    form.content = row.content || row.displayRemark;
    form.categoryName = row.categoryName || row.displayCategory;
    form.sort = Number(row.sort || 0);
    form.status = row.status ?? row.isPublished;
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getArticleDetail(String(row.id));
    currentRow.value = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.title.trim()) {
    message("请输入文章标题", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      title: form.title,
      content: form.content,
      categoryName: form.categoryName,
      sort: form.sort,
      status: form.status
    };
    if (editingRow.value) {
      await updateArticle(String(editingRow.value.id), payload);
      message("文章修改成功", { type: "success" });
    } else {
      await createArticle(payload);
      message("文章新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("文章保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleToggleStatus(row: Record<string, any>) {
  try {
    await updateArticleStatus(String(row.id), !row.isPublished);
    message(row.isPublished ? "文章已下线" : "文章已发布", { type: "success" });
    await loadData();
  } catch (_error) {
    message("文章状态变更失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除文章「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteArticle(String(row.id));
    message("文章删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("文章删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="公告管理"
    description="承接文章台账、文章编辑、上下线和删除动作，作为内容运营里的真实文章治理页。"
    api-path="/manager/other/article/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '已发布', value: '已发布' },
      { label: '未发布', value: '未发布' }
    ]"
    :quick-actions="[
      { label: '文章新增', value: '已接入', type: 'primary' },
      { label: '文章上下线', value: '已接入', type: 'warning' },
      { label: '文章维护', value: '编辑/删除', type: 'success' }
    ]"
    keyword-label="文章标题"
    keyword-placeholder="请输入文章标题"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增文章</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="handleToggleStatus(row)">
        {{ row.isPublished ? "下线" : "发布" }}
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑文章' : '新增文章'" width="720px">
    <el-form label-width="88px">
      <el-form-item label="文章标题" required>
        <el-input v-model="form.title" placeholder="请输入文章标题" />
      </el-form-item>
      <el-form-item label="文章分类">
        <el-input v-model="form.categoryName" placeholder="请输入文章分类名称" />
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="发布状态">
        <el-switch v-model="form.status" />
      </el-form-item>
      <el-form-item label="文章内容">
        <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入文章内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="文章详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="文章标题">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="发布状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="文章分类">{{ currentRow.displayCategory }}</el-descriptions-item>
      <el-descriptions-item label="发布时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="文章内容">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
