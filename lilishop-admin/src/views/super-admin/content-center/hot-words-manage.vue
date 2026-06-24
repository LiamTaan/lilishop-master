<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createHotWord,
  deleteHotWord,
  getHotWordsHistory,
  getHotWordsPage,
  getHotWordsStatistics
} from "@/api/super-admin";
import { extractApiPayload } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const historyVisible = ref(false);
const rows = ref<Record<string, any>[]>([]);
const historyRows = ref<Record<string, any>[]>([]);
const statistics = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  date: ""
});
const form = reactive({
  words: "",
  score: 100
});

const columns: TableColumnList = [
  { label: "热词", prop: "displayName", minWidth: 220 },
  { label: "热度值", prop: "displayScore", minWidth: 120 },
  { label: "排序值", prop: "displaySort", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "备注", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 140, fixed: "right", slot: "operation" }
];

const historyColumns: TableColumnList = [
  { label: "历史热词", prop: "displayName", minWidth: 220 },
  { label: "热度值", prop: "displayScore", minWidth: 120 },
  { label: "排序值", prop: "displaySort", minWidth: 120 },
  { label: "记录时间", prop: "displayTime", minWidth: 180 }
];

const summaryCards = computed(() => [
  {
    label: "当前热词数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前平台热词列表"
  },
  {
    label: "最高热度",
    value: rows.value[0]?.displayScore || statistics.value.maxScore || 0,
    accent: "green" as const,
    hint: "用于观察热词峰值"
  },
  {
    label: "统计状态",
    value: Object.keys(statistics.value).length ? "已接入" : "待联调",
    accent: "blue" as const,
    hint: "承接热词统计接口"
  },
  {
    label: "治理动作",
    value: "可新增/删除",
    accent: "purple" as const,
    hint: "使用真实热词接口"
  }
]);

function normalizeHotWord(item: Record<string, any>, index: number) {
  const word =
    item.words || item.word || item.name || item.keyword || `热词-${index + 1}`;
  const score = item.score ?? item.hotScore ?? item.frequency ?? item.sort ?? 0;
  const sort = item.sort ?? index + 1;
  return {
    ...item,
    id: item.id || word,
    words: word,
    displayName: word,
    displayScore: score,
    displaySort: sort,
    displayTime: item.updateTime || item.createTime || item.dateTime || "-",
    displayRemark: item.remark || item.source || "热词治理记录"
  };
}

function buildStatisticsView(payload: Record<string, any>) {
  const list = Array.isArray(payload?.list)
    ? payload.list
    : Array.isArray(payload?.records)
      ? payload.records
      : Array.isArray(payload)
        ? payload
        : [];
  const scores = rows.value
    .map(item => Number(item.displayScore) || 0)
    .filter(score => score > 0);
  return {
    raw: payload,
    total: payload?.total ?? rows.value.length,
    maxScore: payload?.maxScore ?? (scores.length ? Math.max(...scores) : 0),
    items: list
  };
}

async function loadHotWords() {
  loading.value = true;
  try {
    const [wordRes, statRes] = await Promise.all([
      getHotWordsPage(),
      getHotWordsStatistics()
    ]);
    const hotWordPayload = extractApiPayload<any>(wordRes);
    const hotWordList = Array.isArray(hotWordPayload) ? hotWordPayload : [];
    const normalized = hotWordList.map(normalizeHotWord);
    rows.value = query.keyword
      ? normalized.filter(item => item.displayName.includes(query.keyword.trim()))
      : normalized;
    statistics.value = buildStatisticsView(
      (extractApiPayload<any>(statRes) || {}) as Record<string, any>
    );
  } catch (_error) {
    message("热词列表加载失败，请稍后重试", { type: "error" });
  } finally {
    loading.value = false;
  }
}

async function loadHistory() {
  if (!query.date) {
    message("请先选择历史日期", { type: "warning" });
    return;
  }
  try {
    const res = await getHotWordsHistory({ date: query.date });
    const payload = extractApiPayload<any>(res);
    const list = Array.isArray(payload) ? payload : [];
    historyRows.value = list.map(normalizeHotWord);
    historyVisible.value = true;
  } catch (_error) {
    message("历史热词加载失败，请检查日期后重试", { type: "error" });
  }
}

async function handleCreate() {
  if (!form.words.trim()) {
    message("请输入热词内容", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    await createHotWord({
      words: form.words.trim(),
      score: form.score
    });
    message("热词新增成功", { type: "success" });
    dialogVisible.value = false;
    form.words = "";
    form.score = 100;
    await loadHotWords();
  } catch (_error) {
    message("热词新增失败，请确认后端字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除热词「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteHotWord(String(row.words || row.displayName));
    message("热词删除成功", { type: "success" });
    await loadHotWords();
  } catch (_error) {
    message("热词删除失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string }) {
  query.keyword = payload.keyword || "";
  loadHotWords();
}

function handleReset() {
  query.keyword = "";
  query.date = "";
  loadHotWords();
}

onMounted(() => {
  loadHotWords();
});
</script>

<template>
  <WholesaleAdminPage
    title="热词治理"
    description="承接平台热词列表、热词治理动作、历史热词和热词统计视图，作为内容运营侧的真实联调页面。"
    api-path="/manager/hotwords/hotwords"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '热词动作', value: '新增/删除', type: 'primary' },
      { label: '历史热词', value: '已承接', type: 'success' },
      { label: '统计能力', value: '已接入', type: 'warning' }
    ]"
    keyword-label="热词检索"
    keyword-placeholder="请输入热词内容"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="历史日期">
        <el-date-picker
          v-model="query.date"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择日期"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button plain @click="loadHistory">查看历史</el-button>
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button :loading="loading" plain @click="loadHotWords">刷新热词</el-button>
      <el-button type="primary" @click="dialogVisible = true">新增热词</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <section class="stats-panel">
    <div class="stats-panel__header">
      <div>
        <h3>热词统计视图</h3>
        <p>使用后端热词统计接口承接运营统计，不额外发明前端字段语义。</p>
      </div>
      <el-tag type="success" effect="light" round>
        {{ Object.keys(statistics).length ? "真实统计" : "待联调" }}
      </el-tag>
    </div>
    <div class="stats-panel__grid">
      <article class="stats-card">
        <span>统计总数</span>
        <strong>{{ statistics.total || rows.length }}</strong>
      </article>
      <article class="stats-card">
        <span>最高热度</span>
        <strong>{{ statistics.maxScore || 0 }}</strong>
      </article>
      <article class="stats-card">
        <span>统计项数</span>
        <strong>{{ Array.isArray(statistics.items) ? statistics.items.length : 0 }}</strong>
      </article>
    </div>
    <el-table
      v-if="Array.isArray(statistics.items) && statistics.items.length"
      :data="statistics.items"
      border
      class="stats-panel__table"
    >
      <el-table-column label="关键词" min-width="220">
        <template #default="{ row }">
          {{ row.words || row.word || row.keyword || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="统计值" min-width="120">
        <template #default="{ row }">
          {{ row.score || row.count || row.value || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="时间" min-width="180">
        <template #default="{ row }">
          {{ row.date || row.createTime || row.updateTime || "-" }}
        </template>
      </el-table-column>
    </el-table>
  </section>

  <el-dialog v-model="dialogVisible" title="新增热词" width="520px">
    <el-form label-width="88px">
      <el-form-item label="热词内容" required>
        <el-input v-model="form.words" placeholder="请输入热词内容" maxlength="50" />
      </el-form-item>
      <el-form-item label="热度值">
        <el-input-number v-model="form.score" :min="1" :max="999999" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleCreate">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="historyVisible" title="历史热词" size="720px">
    <p class="history-tip">
      当前日期：{{ query.date || "未选择" }}，使用 `/manager/hotwords/hotwords/history` 拉取历史热词。
    </p>
    <pure-table row-key="id" :data="historyRows" :columns="historyColumns" />
  </el-drawer>
</template>

<style scoped>
.stats-panel {
  padding: 20px 22px;
  border: 1px solid #f2e8de;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #fffaf4 100%);
  box-shadow: 0 10px 26px rgb(39 23 6 / 4%);
}

.stats-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.stats-panel__header h3 {
  margin: 0 0 6px;
  color: #2d2f33;
}

.stats-panel__header p {
  margin: 0;
  color: #8f6d4e;
  font-size: 13px;
}

.stats-panel__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stats-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border: 1px solid #f2e6da;
  border-radius: 16px;
  background: #fff;
}

.stats-card span {
  color: #8a8f98;
  font-size: 13px;
}

.stats-card strong {
  font-size: 28px;
  color: #2d2f33;
}

.stats-panel__table {
  margin-top: 12px;
}

.history-tip {
  margin: 0 0 12px;
  color: #8a6b50;
  font-size: 13px;
}
</style>
