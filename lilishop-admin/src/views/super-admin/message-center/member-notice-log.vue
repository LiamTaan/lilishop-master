<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  deleteMemberNoticeLog,
  getMemberNoticeLogDetail,
  getMemberNoticeLogPage
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
  { label: "通知对象", prop: "displayName", minWidth: 220 },
  { label: "发送结果", prop: "displayStatus", minWidth: 120 },
  { label: "发送时间", prop: "displayTime", minWidth: 180 },
  { label: "失败原因", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 160, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "日志总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前通知日志总量"
  },
  {
    label: "成功发送",
    value: rows.value.filter(item => String(item.displayStatus).includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "发送成功记录"
  },
  {
    label: "失败记录",
    value: rows.value.filter(item => !String(item.displayStatus).includes("SUCCESS")).length,
    accent: "blue" as const,
    hint: "发送失败或异常"
  },
  {
    label: "治理动作",
    value: "详情/删除",
    accent: "purple" as const,
    hint: "承接真实日志接口"
  }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.logId || item.sn,
    displayName:
      item.title || item.noticeTitle || item.memberName || item.nickname || "-",
    displayStatus: item.status || item.sendStatus || item.result || "UNKNOWN",
    displayTime: item.createTime || item.sendTime || item.updateTime || "-",
    displayRemark: item.errorMessage || item.remark || item.content || "-"
  };
}

async function loadData() {
  try {
    const res = await getMemberNoticeLogPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(item => item.displayName.includes(query.keyword));
    }
    if (query.status) {
      list = list.filter(item => String(item.displayStatus) === query.status);
    }
    rows.value = list;
  } catch (_error) {
    message("通知日志加载失败，请稍后重试", { type: "error" });
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

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getMemberNoticeLogDetail(String(row.id));
    currentRow.value = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除通知日志「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberNoticeLog([String(row.id)]);
    message("通知日志删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("通知日志删除失败", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="消息监控"
    description="承接通知发送结果日志、失败原因台账、详情抽屉和清理动作，方便联调时快速定位消息问题。"
    api-path="/manager/message/memberNoticeLog/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '日志详情', value: '已接入', type: 'primary' },
      { label: '失败原因', value: '已展示', type: 'warning' },
      { label: '日志删除', value: '已接入', type: 'success' }
    ]"
    keyword-label="通知对象"
    keyword-placeholder="请输入会员昵称或通知标题"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="通知日志详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="通知对象">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="发送结果">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="发送时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="失败原因">
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
