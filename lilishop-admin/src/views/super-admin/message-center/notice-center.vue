<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import {
  createMessageChannel,
  deleteMessageChannel,
  getMemberMessagePage,
  getMemberPage,
  getMessageChannelPage,
  getNoticeTemplateDetail,
  getNoticeTemplatePage,
  getStoreMessagePage,
  updateNoticeTemplate,
  updateNoticeTemplateStatus
} from "@/api/super-admin";
import { getStoreApplyPage } from "@/api/store-governance";
import {
  extractApiRecords,
  formatAdminDateTime,
  getEnableStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

type ReceiverType = "ALL" | "CONSUMER" | "AGENT" | "SUPPLIER" | "STORE";
type TargetType = "MEMBER" | "STORE";

type TargetOption = {
  key: string;
  id: string;
  name: string;
  targetType: TargetType;
  receiverLabel: string;
  secondaryText: string;
};

const activeTab = ref("template");
const templateLoading = ref(false);
const manualLoading = ref(false);
const memberLoading = ref(false);
const storeLoading = ref(false);
const candidateLoading = ref(false);

const templateRows = ref<Record<string, any>[]>([]);
const manualRows = ref<Record<string, any>[]>([]);
const memberRows = ref<Record<string, any>[]>([]);
const storeRows = ref<Record<string, any>[]>([]);

const templateDialogVisible = ref(false);
const manualDialogVisible = ref(false);
const candidateDialogVisible = ref(false);
const candidateTableRef = ref();

const selectedTargets = ref<TargetOption[]>([]);
const candidateRows = ref<TargetOption[]>([]);

const templateForm = reactive({
  id: "",
  title: "",
  content: ""
});

const manualForm = reactive({
  title: "",
  content: "",
  bizType: "PLATFORM",
  messageClient: "ALL" as ReceiverType,
  messageRange: "ALL"
});

const templateQuery = reactive({
  type: ""
});

const manualQuery = reactive({
  title: "",
  bizType: ""
});

const resultQuery = reactive({
  bizType: ""
});

const candidateQuery = reactive({
  keyword: ""
});

const bizTypeOptions = [
  { label: "交易通知", value: "TRADE" },
  { label: "售后通知", value: "AFTER_SALE" },
  { label: "资产通知", value: "ASSET" },
  { label: "活动通知", value: "ACTIVITY" },
  { label: "平台公告", value: "PLATFORM" },
  { label: "店铺治理通知", value: "STORE_GOVERNANCE" }
];

const receiverOptions: Array<{ label: string; value: ReceiverType }> = [
  { label: "全部", value: "ALL" },
  { label: "消费者", value: "CONSUMER" },
  { label: "代理商", value: "AGENT" },
  { label: "供货商", value: "SUPPLIER" },
  { label: "店铺", value: "STORE" }
];

const sendRangeOptions = [
  { label: "全部", value: "ALL" },
  { label: "指定用户", value: "APPOINT" }
];

const dashboardCards = computed(() => [
  { label: "业务模板", value: templateRows.value.length, hint: "自动触发通知模板" },
  { label: "手工通知", value: manualRows.value.length, hint: "管理端发送记录" },
  { label: "会员结果", value: memberRows.value.length, hint: "会员通知结果" },
  { label: "店铺结果", value: storeRows.value.length, hint: "店铺通知结果" }
]);

const selectedTargetSummary = computed(() => {
  if (selectedTargets.value.length === 0) {
    return "未选择对象";
  }
  return `已选择 ${selectedTargets.value.length} 个对象`;
});

function formatBizType(value?: string) {
  return bizTypeOptions.find(item => item.value === value)?.label ?? value ?? "-";
}

function formatReceiverType(value?: string) {
  if (value === "MEMBER") return "会员";
  return receiverOptions.find(item => item.value === value)?.label ?? value ?? "-";
}

function normalizeTemplate(item: Record<string, any>) {
  const notice = item.notice ?? item;
  return {
    ...item,
    id: String(notice.id ?? item.id ?? ""),
    displayCode: notice.sceneCode || notice.noticeNode || "-",
    displayTitle: notice.noticeTitle || "-",
    displayContent: notice.noticeContent || "-",
    displayStatus: getEnableStatusLabel(notice.noticeStatus),
    rawStatus: notice.noticeStatus || "",
    displayNode: notice.noticeNode || "-"
  };
}

function normalizeManual(item: Record<string, any>) {
  return {
    ...item,
    displayTitle: item.title || "-",
    displayContent: item.content || "-",
    displayBizType: formatBizType(item.bizType),
    displayClient: formatReceiverType(item.messageClient),
    displayRange:
      item.messageRange === "APPOINT"
        ? "指定用户"
        : item.messageRange === "ALL"
          ? "全部"
          : "-",
    displayTime: formatAdminDateTime(item.createTime)
  };
}

function normalizeResult(item: Record<string, any>, receiver: "member" | "store") {
  return {
    ...item,
    displayReceiver:
      receiver === "member"
        ? item.memberName || item.memberId || "-"
        : item.storeName || item.storeId || "-",
    displayBizType: formatBizType(item.bizType),
    displayStatus: item.status || "-",
    displayTime: formatAdminDateTime(item.createTime)
  };
}

function resetManualForm() {
  manualForm.title = "";
  manualForm.content = "";
  manualForm.bizType = "PLATFORM";
  manualForm.messageClient = "ALL";
  manualForm.messageRange = "ALL";
  selectedTargets.value = [];
  candidateQuery.keyword = "";
}

function openManualDialog() {
  resetManualForm();
  manualDialogVisible.value = true;
}

function normalizeMemberTargets(records: Record<string, any>[]) {
  return records
    .filter(item => {
      const isAgent = Boolean(item.agent);
      const isSupplier = Boolean(item.supplier);
      switch (manualForm.messageClient) {
        case "CONSUMER":
          return !isAgent && !isSupplier;
        case "AGENT":
          return isAgent;
        case "SUPPLIER":
          return isSupplier;
        case "ALL":
          return true;
        default:
          return false;
      }
    })
    .map(item => {
      const receiverLabel = item.agent
        ? "代理商"
        : item.supplier
          ? "供货商"
          : "消费者";
      return {
        key: `MEMBER:${item.id}`,
        id: String(item.id),
        name: item.nickName || item.username || item.mobile || String(item.id),
        targetType: "MEMBER" as const,
        receiverLabel,
        secondaryText: item.mobile || item.username || "-"
      };
    });
}

function normalizeStoreTargets(records: Record<string, any>[]) {
  return records.map(item => ({
    key: `STORE:${item.id}`,
    id: String(item.id),
    name: item.storeName || String(item.id),
    targetType: "STORE" as const,
    receiverLabel: "店铺",
    secondaryText: item.memberName || item.storeType || "-"
  }));
}

async function loadCandidateRows() {
  candidateLoading.value = true;
  try {
    const keyword = candidateQuery.keyword.trim();
    const tasks: Promise<any>[] = [];
    const needMemberTargets = ["ALL", "CONSUMER", "AGENT", "SUPPLIER"].includes(
      manualForm.messageClient
    );
    const needStoreTargets = ["ALL", "STORE"].includes(manualForm.messageClient);

    if (needMemberTargets) {
      tasks.push(
        getMemberPage({
          pageNumber: 1,
          pageSize: 200,
          disabled: "OPEN",
          username: keyword || undefined,
          mobile: keyword || undefined,
          nickName: keyword || undefined
        })
      );
    }

    if (needStoreTargets) {
      tasks.push(
        getStoreApplyPage({
          pageNumber: 1,
          pageSize: 200,
          includeAll: true,
          storeDisable: "OPEN",
          storeName: keyword || undefined,
          memberName: keyword || undefined
        })
      );
    }

    const responses = await Promise.all(tasks);
    let cursor = 0;
    const nextRows: TargetOption[] = [];

    if (needMemberTargets) {
      nextRows.push(...normalizeMemberTargets(extractApiRecords(responses[cursor++])));
    }
    if (needStoreTargets) {
      nextRows.push(...normalizeStoreTargets(extractApiRecords(responses[cursor++])));
    }

    candidateRows.value = nextRows;
  } finally {
    candidateLoading.value = false;
  }
}

async function openCandidateDialog() {
  await loadCandidateRows();
  candidateDialogVisible.value = true;
}

function handleCandidateSelection(rows: TargetOption[]) {
  selectedTargets.value = rows;
}

function restoreCandidateSelection() {
  const selectedKeySet = new Set(selectedTargets.value.map(item => item.key));
  candidateRows.value.forEach(row => {
    candidateTableRef.value?.toggleRowSelection(row, selectedKeySet.has(row.key));
  });
}

async function loadTemplates() {
  templateLoading.value = true;
  try {
    const res = await getNoticeTemplatePage({
      pageNumber: 1,
      pageSize: 200,
      type: templateQuery.type || undefined
    });
    templateRows.value = extractApiRecords(res).map(normalizeTemplate);
  } finally {
    templateLoading.value = false;
  }
}

async function loadManualMessages() {
  manualLoading.value = true;
  try {
    const res = await getMessageChannelPage({
      pageNumber: 1,
      pageSize: 200,
      title: manualQuery.title || undefined,
      bizType: manualQuery.bizType || undefined
    });
    manualRows.value = extractApiRecords(res).map(normalizeManual);
  } finally {
    manualLoading.value = false;
  }
}

async function loadMemberResults() {
  memberLoading.value = true;
  try {
    const res = await getMemberMessagePage({
      pageNumber: 1,
      pageSize: 200,
      bizType: resultQuery.bizType || undefined
    });
    memberRows.value = extractApiRecords(res).map(item => normalizeResult(item, "member"));
  } finally {
    memberLoading.value = false;
  }
}

async function loadStoreResults() {
  storeLoading.value = true;
  try {
    const res = await getStoreMessagePage({
      pageNumber: 1,
      pageSize: 200,
      bizType: resultQuery.bizType || undefined
    });
    storeRows.value = extractApiRecords(res).map(item => normalizeResult(item, "store"));
  } finally {
    storeLoading.value = false;
  }
}

async function loadAll() {
  await Promise.all([
    loadTemplates(),
    loadManualMessages(),
    loadMemberResults(),
    loadStoreResults()
  ]);
}

async function openEditTemplate(row: Record<string, any>) {
  const res = await getNoticeTemplateDetail(String(row.id));
  const detail = (res as any)?.data ?? {};
  templateForm.id = String(row.id);
  templateForm.title = detail.noticeTitle || row.displayTitle || "";
  templateForm.content = detail.noticeContent || row.displayContent || "";
  templateDialogVisible.value = true;
}

async function saveTemplate() {
  await updateNoticeTemplate(templateForm.id, {
    noticeTitle: templateForm.title,
    noticeContent: templateForm.content
  });
  message("业务通知模板已更新", { type: "success" });
  templateDialogVisible.value = false;
  await loadTemplates();
}

async function toggleTemplateStatus(row: Record<string, any>) {
  const nextStatus = row.rawStatus === "OPEN" ? "CLOSE" : "OPEN";
  await updateNoticeTemplateStatus(String(row.id), nextStatus);
  message("模板状态已更新", { type: "success" });
  await loadTemplates();
}

async function submitManualMessage() {
  if (!manualForm.title.trim()) {
    message("请输入通知标题", { type: "warning" });
    return;
  }
  if (!manualForm.content.trim()) {
    message("请输入通知内容", { type: "warning" });
    return;
  }
  if (manualForm.messageRange === "APPOINT" && selectedTargets.value.length === 0) {
    message("请先选择指定对象", { type: "warning" });
    return;
  }

  const payload: Record<string, any> = {
    title: manualForm.title,
    content: manualForm.content,
    bizType: manualForm.bizType,
    messageClient: manualForm.messageClient,
    messageRange: manualForm.messageRange
  };

  if (manualForm.messageRange === "APPOINT") {
    payload.userIds = selectedTargets.value.map(item => item.id);
    payload.userNames = selectedTargets.value.map(item => item.name);
    payload.userTypes = selectedTargets.value.map(item => item.targetType);
  }

  await createMessageChannel(payload);
  message("手工通知已发送", { type: "success" });
  manualDialogVisible.value = false;
  resetManualForm();
  await Promise.all([loadManualMessages(), loadMemberResults(), loadStoreResults()]);
}

async function removeManualMessage(id: string) {
  await deleteMessageChannel(id);
  message("通知记录已删除", { type: "success" });
  await loadManualMessages();
}

watch(
  () => manualForm.messageClient,
  () => {
    selectedTargets.value = [];
  }
);

watch(
  () => manualForm.messageRange,
  value => {
    if (value !== "APPOINT") {
      selectedTargets.value = [];
    }
  }
);

watch(candidateDialogVisible, async value => {
  if (value) {
    await nextTick();
    restoreCandidateSelection();
  }
});

onMounted(loadAll);
</script>

<template>
  <div class="notice-center-page">
    <div class="page-header">
      <div>
        <h2>站内通知</h2>
        <p>统一承接业务通知模板、手工发送，以及会员和店铺的收件结果。</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadAll">刷新</el-button>
        <el-button type="primary" @click="openManualDialog">发送手工通知</el-button>
      </div>
    </div>

    <div class="dashboard-grid">
      <div v-for="card in dashboardCards" :key="card.label" class="dashboard-card">
        <span class="card-label">{{ card.label }}</span>
        <strong class="card-value">{{ card.value }}</strong>
        <span class="card-hint">{{ card.hint }}</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="notice-tabs">
      <el-tab-pane label="业务通知模板" name="template">
        <div class="toolbar">
          <el-select v-model="templateQuery.type" clearable placeholder="筛选模板类型" style="width: 220px">
            <el-option label="短信通知" value="SMS" />
            <el-option label="站内通知" value="NOTICE" />
          </el-select>
          <el-button @click="loadTemplates">查询</el-button>
        </div>
        <el-table :data="templateRows" v-loading="templateLoading" border>
          <el-table-column label="模板编码" prop="displayCode" min-width="180" />
          <el-table-column label="模板节点" prop="displayNode" min-width="180" />
          <el-table-column label="模板标题" prop="displayTitle" min-width="220" />
          <el-table-column label="模板内容" prop="displayContent" min-width="320" show-overflow-tooltip />
          <el-table-column label="状态" prop="displayStatus" width="120" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditTemplate(row)">编辑</el-button>
              <el-button link type="primary" @click="toggleTemplateStatus(row)">
                {{ row.rawStatus === "OPEN" ? "停用" : "启用" }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="手工通知发送" name="manual">
        <div class="toolbar">
          <el-input v-model="manualQuery.title" clearable placeholder="按标题搜索" style="width: 240px" />
          <el-select v-model="manualQuery.bizType" clearable placeholder="选择业务分类" style="width: 220px">
            <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadManualMessages">查询</el-button>
        </div>
        <el-table :data="manualRows" v-loading="manualLoading" border>
          <el-table-column label="标题" prop="displayTitle" min-width="220" />
          <el-table-column label="分类" prop="displayBizType" min-width="150" />
          <el-table-column label="接收端" prop="displayClient" width="140" />
          <el-table-column label="发送范围" prop="displayRange" width="120" />
          <el-table-column label="内容" prop="displayContent" min-width="320" show-overflow-tooltip />
          <el-table-column label="发送时间" prop="displayTime" min-width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeManualMessage(String(row.id))">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="会员通知结果" name="member">
        <div class="toolbar">
          <el-select v-model="resultQuery.bizType" clearable placeholder="选择业务分类" style="width: 220px">
            <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadMemberResults">查询</el-button>
        </div>
        <el-table :data="memberRows" v-loading="memberLoading" border>
          <el-table-column label="会员" prop="displayReceiver" min-width="180" />
          <el-table-column label="分类" prop="displayBizType" min-width="150" />
          <el-table-column label="标题" prop="title" min-width="220" />
          <el-table-column label="内容" prop="content" min-width="320" show-overflow-tooltip />
          <el-table-column label="状态" prop="displayStatus" width="120" />
          <el-table-column label="发送时间" prop="displayTime" min-width="180" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="店铺通知结果" name="store">
        <div class="toolbar">
          <el-select v-model="resultQuery.bizType" clearable placeholder="选择业务分类" style="width: 220px">
            <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadStoreResults">查询</el-button>
        </div>
        <el-table :data="storeRows" v-loading="storeLoading" border>
          <el-table-column label="店铺" prop="displayReceiver" min-width="180" />
          <el-table-column label="分类" prop="displayBizType" min-width="150" />
          <el-table-column label="标题" prop="title" min-width="220" />
          <el-table-column label="内容" prop="content" min-width="320" show-overflow-tooltip />
          <el-table-column label="状态" prop="displayStatus" width="120" />
          <el-table-column label="发送时间" prop="displayTime" min-width="180" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="templateDialogVisible" title="编辑业务通知模板" width="720px">
      <el-form label-width="100px">
        <el-form-item label="模板标题">
          <el-input v-model="templateForm.title" />
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input v-model="templateForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="manualDialogVisible" title="发送手工通知" width="760px">
      <el-form label-width="100px">
        <el-form-item label="通知标题">
          <el-input v-model="manualForm.title" />
        </el-form-item>
        <el-form-item label="通知分类">
          <el-select v-model="manualForm.bizType" style="width: 100%">
            <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收端">
          <el-select v-model="manualForm.messageClient" style="width: 100%">
            <el-option v-for="item in receiverOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送范围">
          <el-select v-model="manualForm.messageRange" style="width: 100%">
            <el-option v-for="item in sendRangeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="manualForm.messageRange === 'APPOINT'" label="指定对象">
          <div class="target-picker">
            <div class="target-picker__header">
              <span>{{ selectedTargetSummary }}</span>
              <el-button type="primary" link @click="openCandidateDialog">选择对象</el-button>
            </div>
            <div v-if="selectedTargets.length > 0" class="target-picker__tags">
              <el-tag v-for="item in selectedTargets" :key="item.key" class="target-picker__tag">
                {{ item.name }} / {{ item.receiverLabel }}
              </el-tag>
            </div>
            <div v-else class="target-picker__empty">
              根据接收端选择对象，不能手填 ID 和名称。
            </div>
          </div>
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="manualForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitManualMessage">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="candidateDialogVisible" title="选择通知对象" width="920px">
      <div class="toolbar">
        <el-input v-model="candidateQuery.keyword" clearable placeholder="按名称或手机号搜索" style="width: 260px" />
        <el-button @click="loadCandidateRows">查询</el-button>
      </div>
      <el-table
        ref="candidateTableRef"
        :data="candidateRows"
        row-key="key"
        v-loading="candidateLoading"
        border
        @selection-change="handleCandidateSelection"
      >
        <el-table-column type="selection" width="56" />
        <el-table-column label="对象类型" prop="receiverLabel" width="120" />
        <el-table-column label="名称" prop="name" min-width="220" />
        <el-table-column label="辅助信息" prop="secondaryText" min-width="220" />
        <el-table-column label="目标类型" prop="targetType" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="candidateDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="candidateDialogVisible = false">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.notice-center-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(135deg, #16324f, #2f6b8f);
  color: #fff;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
}

.header-actions {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.dashboard-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px 20px;
  border-radius: 18px;
  background: #fff8ef;
  border: 1px solid #f1d5aa;
}

.card-label {
  font-size: 13px;
  color: #996515;
}

.card-value {
  font-size: 28px;
  color: #3c2a13;
}

.card-hint {
  font-size: 12px;
  color: #7c6750;
}

.notice-tabs {
  padding: 20px;
  border-radius: 20px;
  background: #ffffff;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.target-picker {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: #fbfcfe;
}

.target-picker__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.target-picker__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.target-picker__tag {
  margin: 0;
}

.target-picker__empty {
  margin-top: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
