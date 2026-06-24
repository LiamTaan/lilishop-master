<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMemberNoticeSender,
  deleteMemberNoticeSender,
  getMemberNoticeSenderDetail,
  getMemberNoticeSenderPage
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
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
  title: "",
  content: "",
  sendType: "ALL"
});

const columns: TableColumnList = [
  { label: "任务标题", prop: "displayName", minWidth: 220 },
  { label: "任务状态", prop: "displayStatus", minWidth: 120 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "任务说明", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "任务总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前发送任务总量"
  },
  {
    label: "成功任务",
    value: rows.value.filter(item => String(item.displayStatus).includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "已发送完成"
  },
  {
    label: "待发送任务",
    value: rows.value.filter(item => !String(item.displayStatus).includes("SUCCESS")).length,
    accent: "blue" as const,
    hint: "待后续治理"
  },
  {
    label: "治理动作",
    value: "新建/删除",
    accent: "purple" as const,
    hint: "承接客户消息接口"
  }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.noticeId || item.sn,
    displayName: item.title || item.noticeTitle || "-",
    displayStatus: item.status || item.sendStatus || "WAIT",
    displayTime: item.createTime || item.sendTime || item.updateTime || "-",
    displayRemark: item.content || item.remark || item.operatorName || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.title = query.keyword;
    if (query.status) params.status = query.status;
    const res = await getMemberNoticeSenderPage(params);
    rows.value = extractApiRecords(res).map(normalizeRecord);
  } catch (_error) {
    message("发送任务加载失败，请稍后重试", { type: "error" });
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
  editingId.value = "";
  form.title = "";
  form.content = "";
  form.sendType = "ALL";
  dialogVisible.value = true;
}

async function openResend(row: Record<string, any>) {
  editingId.value = String(row.id);
  try {
    const res = await getMemberNoticeSenderDetail(editingId.value);
    const detail = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
    form.title = detail.title || detail.displayName;
    form.content = detail.content || detail.displayRemark;
  } catch (_error) {
    form.title = row.title || row.displayName;
    form.content = row.content || row.displayRemark;
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.title.trim()) {
    message("请输入任务标题", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    await createMemberNoticeSender({
      id: editingId.value || undefined,
      title: form.title,
      content: form.content,
      sendType: form.sendType
    });
    message(editingId.value ? "发送任务重提成功" : "发送任务创建成功", {
      type: "success"
    });
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("发送任务保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除发送任务「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberNoticeSender([String(row.id)]);
    message("发送任务删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("发送任务删除失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="发送任务"
    description="承接客户消息发送任务台账、新建发送任务、重提发送和删除动作，作为消息通知侧的任务治理页。"
    api-path="/manager/message/memberNoticeSenter/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '创建任务', value: '已接入', type: 'primary' },
      { label: '重提任务', value: '已接入', type: 'success' },
      { label: '删除任务', value: '已接入', type: 'warning' }
    ]"
    keyword-label="任务标题"
    keyword-placeholder="请输入任务标题"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新建发送任务</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openResend(row)">重提发送</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingId ? '重提发送任务' : '新建发送任务'" width="620px">
    <el-form label-width="88px">
      <el-form-item label="任务标题" required>
        <el-input v-model="form.title" placeholder="请输入任务标题" />
      </el-form-item>
      <el-form-item label="发送范围">
        <el-select v-model="form.sendType" style="width: 100%">
          <el-option label="全量会员" value="ALL" />
          <el-option label="指定范围" value="PART" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务内容">
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入任务内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="发送任务详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="任务标题">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="任务状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="发送时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="任务说明">
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
