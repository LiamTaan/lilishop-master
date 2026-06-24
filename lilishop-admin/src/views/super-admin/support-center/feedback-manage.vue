<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { getFeedbackDetail, getFeedbackPage } from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });

const columns: TableColumnList = [
  { label: "反馈用户", prop: "displayName", minWidth: 180 },
  { label: "联系方式", prop: "displayContact", minWidth: 180 },
  { label: "提交时间", prop: "displayTime", minWidth: 180 },
  { label: "反馈内容", prop: "displayRemark", minWidth: 280, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 120, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "反馈总数", value: rows.value.length, accent: "orange" as const, hint: "当前用户反馈台账" },
  { label: "有联系方式", value: rows.value.filter(item => item.displayContact !== "-").length, accent: "green" as const, hint: "便于回访处理" },
  { label: "待人工查看", value: rows.value.length, accent: "blue" as const, hint: "当前以台账承接为主" },
  { label: "承接能力", value: "分页/详情", accent: "purple" as const, hint: "后端当前提供查询能力" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.feedbackId || item.mobile || item.memberName,
    displayName: item.memberName || item.username || item.nickName || "-",
    displayContact: item.mobile || item.contact || item.email || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.content || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getFeedbackPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(
        item =>
          item.displayName.includes(query.keyword) ||
          item.displayContact.includes(query.keyword) ||
          item.displayRemark.includes(query.keyword)
      );
    }
    rows.value = list;
  } catch (_error) {
    message("意见反馈加载失败，请稍后重试", { type: "error" });
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

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getFeedbackDetail(String(row.id));
    currentRow.value = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="意见反馈"
    description="承接用户意见反馈台账和反馈详情抽屉，作为平台支撑侧的问题收集页。"
    api-path="/manager/other/feedback"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '反馈台账', value: '已接入', type: 'primary' },
      { label: '反馈详情', value: '已接入', type: 'success' },
      { label: '治理动作', value: '待后端扩展', type: 'warning' }
    ]"
    keyword-label="用户/联系方式/内容"
    keyword-placeholder="请输入反馈用户、联系方式或反馈内容"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="反馈详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="反馈用户">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{ currentRow.displayContact }}</el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="反馈内容">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
