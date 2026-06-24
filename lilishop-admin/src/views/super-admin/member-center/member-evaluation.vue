<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  deleteMemberEvaluation,
  getMemberEvaluationDetail,
  getMemberEvaluationPage,
  updateMemberEvaluationStatus,
  updateMemberEvaluationTop
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });

const columns: TableColumnList = [
  { label: "用户名称", prop: "displayName", minWidth: 180 },
  { label: "评价状态", prop: "displayStatus", minWidth: 120 },
  { label: "商品标题", prop: "displayGoods", minWidth: 220 },
  { label: "评价时间", prop: "displayTime", minWidth: 180 },
  { label: "评价内容", prop: "displayRemark", minWidth: 280, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 280, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "评价总数", value: rows.value.length, accent: "orange" as const, hint: "当前评价台账" },
  { label: "正常评价", value: rows.value.filter(item => item.isOpen).length, accent: "green" as const, hint: "可见评价" },
  { label: "置顶评价", value: rows.value.filter(item => item.isTop).length, accent: "blue" as const, hint: "重点展示评价" },
  { label: "治理动作", value: "详情/上下线/置顶/删除", accent: "purple" as const, hint: "评价治理已承接" }
]);

function normalizeRecord(item: Record<string, any>) {
  const isOpen = String(item.status || item.auditStatus || "OPEN").toUpperCase().includes("OPEN");
  return {
    ...item,
    id: item.id || item.evaluationId,
    isOpen,
    isTop: Boolean(item.top),
    displayName: item.memberName || item.nickname || item.username || "-",
    displayStatus: isOpen ? "正常" : "关闭",
    displayGoods: item.goodsName || item.skuName || item.goodsTitle || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.content || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.memberName = query.keyword;
    if (query.status) params.status = query.status === "正常" ? "OPEN" : "CLOSE";
    rows.value = extractApiRecords(await getMemberEvaluationPage(params)).map(normalizeRecord);
  } catch (_error) {
    message("用户评价加载失败，请稍后重试", { type: "error" });
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
    const res = await getMemberEvaluationDetail(String(row.id));
    currentRow.value = normalizeRecord(extractApiPayload<Record<string, any>>(res) || row);
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

async function handleToggleStatus(row: Record<string, any>) {
  try {
    await updateMemberEvaluationStatus(String(row.id), row.isOpen ? "CLOSE" : "OPEN");
    message(row.isOpen ? "评价已关闭" : "评价已恢复", { type: "success" });
    await loadData();
  } catch (_error) {
    message("评价状态变更失败", { type: "error" });
  }
}

async function handleToggleTop(row: Record<string, any>) {
  try {
    await updateMemberEvaluationTop(String(row.id), !row.isTop);
    message(row.isTop ? "评价已取消置顶" : "评价已置顶", { type: "success" });
    await loadData();
  } catch (_error) {
    message("评价置顶操作失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除评价「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberEvaluation(String(row.id));
    message("评价删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("评价删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="用户评价"
    description="承接用户评价详情、状态治理、置顶管理和删除动作，作为前台评价审核与展示的治理页。"
    api-path="/manager/member/evaluation/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '正常', value: '正常' },
      { label: '关闭', value: '关闭' }
    ]"
    :quick-actions="[
      { label: '评价详情', value: '已接入', type: 'primary' },
      { label: '状态治理', value: '开启/关闭', type: 'warning' },
      { label: '置顶治理', value: '已接入', type: 'success' }
    ]"
    keyword-label="用户名称"
    keyword-placeholder="请输入用户名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="warning" @click="handleToggleStatus(row)">
        {{ row.isOpen ? "关闭" : "开启" }}
      </el-button>
      <el-button link type="success" @click="handleToggleTop(row)">
        {{ row.isTop ? "取消置顶" : "置顶" }}
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="用户评价详情" size="640px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="用户名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="评价状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="商品标题">{{ currentRow.displayGoods }}</el-descriptions-item>
      <el-descriptions-item label="评价时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="评价内容">{{ currentRow.displayRemark }}</el-descriptions-item>
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
