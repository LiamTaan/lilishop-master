<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  deleteAllMemberNotice,
  deleteMemberNotice,
  getMemberNoticeDetail,
  getMemberNoticePage,
  readAllMemberNotice,
  readMemberNotice
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});

const columns: TableColumnList = [
  { label: "通知标题", prop: "displayName", minWidth: 220 },
  { label: "阅读状态", prop: "displayStatus", minWidth: 120 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "通知内容", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "通知总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前站内通知记录"
  },
  {
    label: "已读通知",
    value: rows.value.filter(item => item.isRead).length,
    accent: "green" as const,
    hint: "已完成阅读"
  },
  {
    label: "未读通知",
    value: rows.value.filter(item => !item.isRead).length,
    accent: "blue" as const,
    hint: "待处理消息"
  },
  {
    label: "治理动作",
    value: "已读/删除",
    accent: "purple" as const,
    hint: "承接真实站内信接口"
  }
]);

function normalizeRecord(item: Record<string, any>) {
  const isRead =
    item.readStatus === true ||
    String(item.status || item.readStatus || "")
      .toUpperCase()
      .includes("READ");
  return {
    ...item,
    id: item.id || item.noticeId || item.sn,
    isRead,
    displayName: item.title || item.noticeTitle || "-",
    displayStatus: isRead ? "已读" : "未读",
    displayTime: item.createTime || item.sendTime || item.updateTime || "-",
    displayRemark: item.content || item.noticeContent || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.title = query.keyword;
    const res = await getMemberNoticePage(params);
    const list = extractApiRecords(res).map(normalizeRecord);
    rows.value = query.status
      ? list.filter(item => item.displayStatus === query.status)
      : list;
  } catch (_error) {
    message("站内通知加载失败，请稍后重试", { type: "error" });
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

async function handleRead(row: Record<string, any>) {
  try {
    await readMemberNotice([String(row.id)]);
    message("通知已标记为已读", { type: "success" });
    await loadData();
  } catch (_error) {
    message("通知已读操作失败", { type: "error" });
  }
}

async function handleReadAll() {
  try {
    await readAllMemberNotice();
    message("全部通知已标记为已读", { type: "success" });
    await loadData();
  } catch (_error) {
    message("全部已读操作失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除通知「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberNotice([String(row.id)]);
    message("通知删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("通知删除失败", { type: "error" });
  }
}

async function handleDeleteAll() {
  await ElMessageBox.confirm("确认清空当前账号全部站内通知吗？", "清空确认", {
    type: "warning"
  });
  try {
    await deleteAllMemberNotice();
    message("全部通知已清空", { type: "success" });
    await loadData();
  } catch (_error) {
    message("全部通知删除失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getMemberNoticeDetail(String(row.id));
    currentRow.value = normalizeRecord((res as any).result || (res as any).data || row);
    detailVisible.value = true;
  } catch (_error) {
    currentRow.value = row;
    detailVisible.value = true;
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="站内通知"
    description="承接会员站内通知台账、已读治理、通知详情和批量清理动作，作为通知中心的真实联调页面。"
    api-path="/manager/message/memberNotice/page"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '未读', value: '未读' },
      { label: '已读', value: '已读' }
    ]"
    :quick-actions="[
      { label: '已读动作', value: '单条/全部', type: 'primary' },
      { label: '删除动作', value: '单条/全部', type: 'warning' },
      { label: '详情抽屉', value: '已接入', type: 'success' }
    ]"
    keyword-label="通知标题"
    keyword-placeholder="请输入通知标题"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button plain @click="handleReadAll">全部已读</el-button>
      <el-button type="danger" plain @click="handleDeleteAll">清空通知</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button v-if="!row.isRead" link type="success" @click="handleRead(row)">
        标记已读
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="通知详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="通知标题">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="阅读状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="发送时间">
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
