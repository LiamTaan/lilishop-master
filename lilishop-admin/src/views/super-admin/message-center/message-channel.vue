<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMessageChannel,
  deleteMessageChannel,
  getMessageChannelPage
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const dialogVisible = ref(false);
const detailVisible = ref(false);
const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  title: "",
  content: "",
  messageType: "NOTICE"
});

const columns: TableColumnList = [
  { label: "消息标题", prop: "displayName", minWidth: 220 },
  { label: "发送状态", prop: "displayStatus", minWidth: 120 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "消息内容", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 200, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "消息总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "已发送",
    value: rows.value.filter(item => String(item.displayStatus).toUpperCase().includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "已成功发送"
  },
  {
    label: "待处理",
    value: rows.value.filter(item => String(item.displayStatus).toUpperCase().includes("WAIT")).length,
    accent: "blue" as const,
    hint: "待执行发送"
  },
  {
    label: "发送动作",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持直接发送消息"
  }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200
  };
  if (query.keyword) params.title = query.keyword;
  if (query.status) params.status = query.status;
  const res = await getMessageChannelPage(params);
  rows.value = extractApiRecords(res).map(item => ({
    ...item,
    id: item.id || item.sn || item.messageId,
    displayName: item.title || item.messageTitle || item.name || "-",
    displayStatus: item.status || item.sendStatus || "-",
    displayTime: item.createTime || item.sendTime || "-",
    displayRemark: item.content || item.remark || "-"
  }));
  selectedRows.value = [];
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
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

async function handleSendMessage() {
  try {
    await createMessageChannel({
      title: form.title,
      content: form.content,
      messageType: form.messageType
    });
    message("消息发送成功", { type: "success" });
    dialogVisible.value = false;
    form.title = "";
    form.content = "";
    form.messageType = "NOTICE";
    await loadData();
  } catch (_error) {
    message("消息发送失败，请检查内容后重试", { type: "error" });
  }
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除消息「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMessageChannel(String(row.id));
    message("消息删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("消息删除失败，请稍后重试", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的消息", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 条消息吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteMessageChannel(id)));
    selectedRows.value = [];
    message("消息批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("消息批量删除失败，请稍后重试", { type: "error" });
  }
}

function exportMessages() {
  if (!rows.value.length) {
    message("暂无可导出的消息发送数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    消息标题: item.displayName,
    发送状态: item.displayStatus,
    发送时间: item.displayTime,
    消息内容: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "消息发送管理");
  writeFile(workbook, "消息发送管理.xlsx");
  message("消息发送数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="消息发送管理"
    description="承接站内消息、发送状态和消息发送治理动作。"
    api-path="/manager/other/message"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '发送动作', value: '已接入', type: 'primary' },
      { label: '消息内容', value: '支持录入', type: 'success' },
      { label: '发送状态', value: '已展示', type: 'warning' }
    ]"
    keyword-label="消息标题"
    keyword-placeholder="请输入消息标题"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportMessages">导出</el-button>
      <el-button type="primary" @click="dialogVisible = true">发送消息</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="primary" @click="form.title = row.displayName; form.content = row.displayRemark; dialogVisible = true">
        复用发送
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" title="发送消息" width="560px">
    <el-form label-width="88px">
      <el-form-item label="消息标题">
        <el-input v-model="form.title" placeholder="请输入消息标题" />
      </el-form-item>
      <el-form-item label="消息类型">
        <el-select v-model="form.messageType" style="width: 100%">
          <el-option label="站内通知" value="NOTICE" />
          <el-option label="服务消息" value="SERVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息内容">
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入消息内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSendMessage">发送</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="消息详情" size="520px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="消息标题">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="发送状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="发送时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="消息内容">
        {{ currentRow.displayRemark }}
      </el-descriptions-item>
      <el-descriptions-item label="原始数据">
        <pre class="whitespace-pre-wrap text-xs">{{ JSON.stringify(currentRow, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
