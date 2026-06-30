<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { getStoreMessagePage } from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });

const columns: TableColumnList = [
  { label: "店铺名称", prop: "displayName", minWidth: 200 },
  { label: "消息类型", prop: "displayStatus", minWidth: 140 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "消息内容", prop: "displayRemark", minWidth: 320, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 120, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "消息总数", value: rows.value.length, accent: "orange" as const, hint: "当前店铺消息台账" },
  { label: "店铺通知", value: rows.value.filter(item => item.displayStatus.includes("店铺")).length, accent: "green" as const, hint: "面向店铺的通知消息" },
  { label: "异常提醒", value: rows.value.filter(item => item.displayRemark.includes("异常")).length, accent: "blue" as const, hint: "包含异常提示的消息" },
  { label: "能力边界", value: "仅分页查询", accent: "purple" as const, hint: "后端暂未提供消息处置动作" }
]);

function normalizeRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.messageId || `${item.storeName}-${item.createTime}`,
    displayName: item.storeName || item.memberName || item.title || "-",
    displayStatus: item.messageType || item.status || item.type || "-",
    displayTime: item.createTime || item.sendTime || "-",
    displayRemark: item.content || item.messageText || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 200
    };
    if (query.keyword) params.content = query.keyword;
    const res = await getStoreMessagePage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("店铺消息加载失败，请稍后重试", { type: "error" });
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

function exportMessages() {
  if (!rows.value.length) {
    message("暂无可导出的店铺消息数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    店铺名称: item.displayName,
    消息类型: item.displayStatus,
    发送时间: item.displayTime,
    消息内容: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "店铺消息");
  writeFile(workbook, "店铺消息.xlsx");
  message("店铺消息导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="店铺消息"
    description="承接店铺消息台账、消息内容查询和详情查看。当前以后端现有分页接口为准，不额外增加前端治理动作。"
    api-path="/manager/other/storeMessage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '分页查询', value: '已接入', type: 'primary' },
      { label: '消息详情', value: '已接入', type: 'success' },
      { label: '台账导出', value: '已接入', type: 'warning' }
    ]"
    keyword-label="消息内容"
    keyword-placeholder="请输入消息内容"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportMessages">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="店铺消息详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="店铺名称">{{ currentRow.displayName }}</el-descriptions-item>
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
