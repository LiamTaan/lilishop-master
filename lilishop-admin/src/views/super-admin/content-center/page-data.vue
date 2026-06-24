<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createPageData,
  deletePageData,
  getPageDataDetail,
  getPageDataPage,
  releasePageData,
  updatePageData
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
  pageName: "",
  pageShow: "",
  pageClientType: "PC"
});

const columns: TableColumnList = [
  { label: "页面名称", prop: "displayName", minWidth: 220 },
  { label: "发布状态", prop: "displayStatus", minWidth: 120 },
  { label: "客户端", prop: "displayClient", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 240, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "页面总数", value: rows.value.length, accent: "orange" as const, hint: "当前装修页面台账" },
  { label: "已发布", value: rows.value.filter(item => String(item.displayStatus).includes("已发布")).length, accent: "green" as const, hint: "已进入线上使用" },
  { label: "待发布", value: rows.value.filter(item => !String(item.displayStatus).includes("已发布")).length, accent: "blue" as const, hint: "待发布或草稿页面" },
  { label: "治理动作", value: "新增/修改/发布/删除", accent: "purple" as const, hint: "承接真实装修接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.pageId || item.pageName,
    displayName: item.pageName || item.name || "-",
    displayStatus: item.release === true || item.pageShow === "ON" || item.status === "RELEASED" ? "已发布" : "待发布",
    displayClient: item.pageClientType || item.clientType || "-",
    displayTime: item.updateTime || item.createTime || "-"
  };
}

async function loadData() {
  try {
    const res = await getPageDataPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("页面装修加载失败，请稍后重试", { type: "error" });
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
  form.pageName = "";
  form.pageShow = "";
  form.pageClientType = "PC";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getPageDataDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.pageName = detail.pageName || detail.displayName;
    form.pageShow = detail.pageShow || "";
    form.pageClientType = detail.pageClientType || detail.displayClient || "PC";
  } catch (_error) {
    form.pageName = row.pageName || row.displayName;
    form.pageShow = row.pageShow || "";
    form.pageClientType = row.pageClientType || row.displayClient || "PC";
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.pageName.trim()) {
    message("请输入页面名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      pageName: form.pageName,
      pageShow: form.pageShow,
      pageClientType: form.pageClientType
    };
    if (editingRow.value) {
      await updatePageData(String(editingRow.value.id), payload);
      message("页面装修修改成功", { type: "success" });
    } else {
      await createPageData(payload);
      message("页面装修新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("页面装修保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleRelease(row: Record<string, any>) {
  try {
    await releasePageData(String(row.id));
    message("页面发布成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("页面发布失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除页面「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deletePageData(String(row.id));
    message("页面删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("页面删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="页面装修"
    description="承接页面装修台账、装修详情、发布动作和页面维护，作为内容运营的真实承接页。"
    api-path="/manager/other/pageData/pageDataList"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[{ label: '待发布', value: '待发布' }, { label: '已发布', value: '已发布' }]"
    :quick-actions="[
      { label: '页面新增', value: '已接入', type: 'primary' },
      { label: '页面发布', value: '已接入', type: 'warning' },
      { label: '页面维护', value: '编辑/删除', type: 'success' }
    ]"
    keyword-label="页面名称"
    keyword-placeholder="请输入页面名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增页面</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="handleRelease(row)">发布</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑页面' : '新增页面'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="页面名称" required>
        <el-input v-model="form.pageName" placeholder="请输入页面名称" />
      </el-form-item>
      <el-form-item label="客户端">
        <el-select v-model="form.pageClientType" style="width: 100%">
          <el-option label="PC" value="PC" />
          <el-option label="H5" value="H5" />
          <el-option label="APP" value="APP" />
        </el-select>
      </el-form-item>
      <el-form-item label="页面标识">
        <el-input v-model="form.pageShow" placeholder="请输入页面标识或展示位" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="页面装修详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="页面名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="发布状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="客户端">{{ currentRow.displayClient }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
