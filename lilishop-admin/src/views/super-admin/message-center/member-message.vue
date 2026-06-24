<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { getMemberMessagePage } from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });

const columns: TableColumnList = [
  { label: "会员名称", prop: "displayName", minWidth: 180 },
  { label: "消息类型", prop: "displayStatus", minWidth: 140 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "消息内容", prop: "displayRemark", minWidth: 320, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 120, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "消息总数", value: rows.value.length, accent: "orange" as const, hint: "当前客户消息台账" },
  { label: "系统通知", value: rows.value.filter(item => item.displayStatus.includes("系统")).length, accent: "green" as const, hint: "系统侧推送消息" },
  { label: "站内消息", value: rows.value.filter(item => item.displayStatus.includes("站内")).length, accent: "blue" as const, hint: "站内信类消息" },
  { label: "能力边界", value: "仅分页查询", accent: "purple" as const, hint: "后端暂未提供消息治理动作" }
]);

function normalizeRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.messageId || `${item.memberName}-${item.createTime}`,
    displayName: item.memberName || item.username || item.mobile || "-",
    displayStatus: item.messageType || item.status || item.type || "-",
    displayTime: item.createTime || item.sendTime || "-",
    displayRemark: item.content || item.messageText || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.content = query.keyword;
    const res = await getMemberMessagePage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("客户消息加载失败，请稍后重试", { type: "error" });
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

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="客户消息"
    description="承接客户消息台账、消息内容查询和消息详情查看。该页以后端现有分页查询为准，不额外发明治理动作。"
    api-path="/manager/other/memberMessage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '分页查询', value: '已接入', type: 'primary' },
      { label: '消息详情', value: '已接入', type: 'success' },
      { label: '治理动作', value: '待后端扩展', type: 'warning' }
    ]"
    keyword-label="消息内容"
    keyword-placeholder="请输入消息内容"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="客户消息详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="会员名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="消息类型">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="发送时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="消息内容">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
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
