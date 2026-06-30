<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createServiceNotice,
  deleteServiceNotice,
  getServiceNoticeDetail,
  getServiceNoticePage,
  updateServiceNotice
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "ServiceNoticeManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  name: "",
  title: "",
  content: "",
  code: "",
  status: "OPEN"
});

const columns: TableColumnList = [
  { label: "通知标题", prop: "displayName", minWidth: 220 },
  { label: "通知状态", prop: "displayStatus", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "通知内容", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "服务通知数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前服务通知模板"
  },
  {
    label: "启用中",
    value: rows.value.filter(item => String(item.displayStatus).includes("OPEN")).length,
    accent: "green" as const,
    hint: "当前启用模板"
  },
  {
    label: "停用中",
    value: rows.value.filter(item => !String(item.displayStatus).includes("OPEN")).length,
    accent: "blue" as const,
    hint: "当前停用模板"
  },
  {
    label: "治理动作",
    value: "新增/编辑/删除",
    accent: "purple" as const,
    hint: "承接真实模板接口"
  }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.noticeId || item.code || item.name,
    displayName: item.title || item.noticeTitle || item.name || "-",
    displayStatus: item.status || item.noticeStatus || "OPEN",
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.content || item.noticeContent || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.title = query.keyword;
    if (query.status) params.status = query.status;
    const res = await getServiceNoticePage(params);
    rows.value = extractApiRecords(res).map(normalizeRecord);
  } catch (_error) {
    message("服务通知加载失败，请稍后重试", { type: "error" });
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  editingId.value = "";
  form.name = "";
  form.title = "";
  form.content = "";
  form.code = "";
  form.status = "OPEN";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingId.value = String(row.id);
  try {
    const res = await getServiceNoticeDetail(editingId.value);
    const detail = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
    form.name = detail.name || detail.displayName;
    form.title = detail.title || detail.displayName;
    form.content = detail.content || detail.displayRemark;
    form.code = detail.code || "";
    form.status = detail.status || detail.displayStatus || "OPEN";
  } catch (_error) {
    form.name = row.name || row.displayName;
    form.title = row.title || row.displayName;
    form.content = row.content || row.displayRemark;
    form.code = row.code || "";
    form.status = row.status || row.displayStatus || "OPEN";
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.title.trim()) {
    message("请输入通知标题", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: form.name || form.title,
      title: form.title,
      content: form.content,
      code: form.code,
      status: form.status
    };
    if (editingId.value) {
      await updateServiceNotice(editingId.value, payload);
      message("服务通知更新成功", { type: "success" });
    } else {
      await createServiceNotice(payload);
      message("服务通知新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("服务通知保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除服务通知「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteServiceNotice([String(row.id)]);
    message("服务通知删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("服务通知删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的服务通知", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 条服务通知吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deleteServiceNotice(selectedIds.value);
    selectedRows.value = [];
    message("服务通知批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("服务通知批量删除失败", { type: "error" });
  }
}

function exportServiceNotices() {
  if (!rows.value.length) {
    message("暂无可导出的服务通知数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    通知标题: item.displayName,
    通知状态: item.displayStatus,
    更新时间: item.displayTime,
    通知内容: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "服务通知");
  writeFile(workbook, "服务通知.xlsx");
  message("服务通知导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="预警设置"
    description="承接平台服务通知模板台账、新增编辑和删除动作，作为消息通知中心的模板治理页。"
    api-path="/manager/message/serviceNotice/page"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '新增模板', value: '已接入', type: 'primary' },
      { label: '编辑模板', value: '已接入', type: 'success' },
      { label: '删除模板', value: '已接入', type: 'warning' }
    ]"
    keyword-label="通知标题"
    keyword-placeholder="请输入通知标题"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportServiceNotices">导出</el-button>
      <el-button type="primary" @click="openCreate">新增服务通知</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑服务通知' : '新增服务通知'" width="620px">
    <el-form label-width="88px">
      <el-form-item label="模板名称">
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="通知标题" required>
        <el-input v-model="form.title" placeholder="请输入通知标题" />
      </el-form-item>
      <el-form-item label="模板编码">
        <el-input v-model="form.code" placeholder="请输入模板编码" />
      </el-form-item>
      <el-form-item label="通知状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="启用" value="OPEN" />
          <el-option label="停用" value="CLOSE" />
        </el-select>
      </el-form-item>
      <el-form-item label="通知内容">
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="服务通知详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="通知标题">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="通知状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="通知内容">
        {{ currentRow.displayRemark }}
      </el-descriptions-item>
      <el-descriptions-item label="原始数据">
        <pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}
</style>
