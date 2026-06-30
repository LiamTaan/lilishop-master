<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createSmsTemplate,
  deleteSmsTemplate,
  getSmsTemplatePage,
  modifySmsTemplate,
  querySmsTemplateStatus
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "SmsTemplateManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  templateName: "",
  templateCode: "",
  templateContent: "",
  templateType: 0
});

const columns: TableColumnList = [
  { label: "模板名称", prop: "displayName", minWidth: 220 },
  { label: "模板状态", prop: "displayStatus", minWidth: 120 },
  { label: "模板编码", prop: "displayCode", minWidth: 180 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "模板总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前短信模板总量"
  },
  {
    label: "审核通过",
    value: rows.value.filter(item => String(item.displayStatus).includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "当前可用模板"
  },
  {
    label: "待审核",
    value: rows.value.filter(item => String(item.displayStatus).includes("WAIT")).length,
    accent: "blue" as const,
    hint: "待同步状态"
  },
  {
    label: "治理动作",
    value: "新增/修改/删除",
    accent: "purple" as const,
    hint: "承接真实模板接口"
  }
]);

const selectedCodes = computed(() =>
  selectedRows.value
    .map(item => String(item.displayCode || item.templateCode || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.templateCode || item.templateName,
    displayName: item.templateName || item.name || "-",
    displayStatus: item.templateStatus || item.status || "UNKNOWN",
    displayCode: item.templateCode || "-",
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.templateContent || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.status) params.templateStatus = Number(query.status);
    const res = await getSmsTemplatePage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(
        item =>
          item.displayName.includes(query.keyword) ||
          String(item.displayCode).includes(query.keyword)
      );
    }
    rows.value = list;
  } catch (_error) {
    message("短信模板加载失败，请稍后重试", { type: "error" });
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  editingRow.value = null;
  form.templateName = "";
  form.templateCode = "";
  form.templateContent = "";
  form.templateType = 0;
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.templateName = row.templateName || row.displayName;
  form.templateCode = row.templateCode || row.displayCode;
  form.templateContent = row.templateContent || row.displayRemark;
  form.templateType = Number(row.templateType || 0);
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.templateName.trim()) {
    message("请输入模板名称", { type: "warning" });
    return;
  }
  if (!form.templateCode.trim()) {
    message("请输入模板编码", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      templateName: form.templateName,
      templateCode: form.templateCode,
      templateContent: form.templateContent,
      templateType: form.templateType
    };
    if (editingRow.value) {
      await modifySmsTemplate(payload);
      message("短信模板修改成功", { type: "success" });
    } else {
      await createSmsTemplate(payload);
      message("短信模板新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("短信模板保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除短信模板「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteSmsTemplate(String(row.displayCode || row.templateCode));
    message("短信模板删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信模板删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedCodes.value.length) {
    message("请先勾选需要删除的短信模板", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedCodes.value.length} 个短信模板吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedCodes.value.map(code => deleteSmsTemplate(code)));
    selectedRows.value = [];
    message("短信模板批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信模板批量删除失败", { type: "error" });
  }
}

async function handleQueryStatus() {
  try {
    await querySmsTemplateStatus();
    message("短信模板状态查询已触发", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信模板状态查询失败", { type: "error" });
  }
}

function exportSmsTemplates() {
  if (!rows.value.length) {
    message("暂无可导出的短信模板数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    模板名称: item.displayName,
    模板状态: item.displayStatus,
    模板编码: item.displayCode,
    更新时间: item.displayTime,
    模板内容: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "短信模板");
  writeFile(workbook, "短信模板.xlsx");
  message("短信模板导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="短信模板"
    description="承接短信模板分页、模板状态查询、模板新增修改删除和详情抽屉，作为短信治理页的真实联调入口。"
    api-path="/manager/sms/template/querySmsTemplatePage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '新增模板', value: '已接入', type: 'primary' },
      { label: '状态查询', value: '已接入', type: 'warning' },
      { label: '修改删除', value: '已接入', type: 'success' }
    ]"
    keyword-label="模板名称/编码"
    keyword-placeholder="请输入模板名称或模板编码"
    :status-options="[
      { label: '待审核', value: '0' },
      { label: '审核通过', value: '1' },
      { label: '审核失败', value: '2' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportSmsTemplates">导出</el-button>
      <el-button plain @click="handleQueryStatus">查询模板状态</el-button>
      <el-button type="primary" @click="openCreate">新增模板</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">修改</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '修改短信模板' : '新增短信模板'" width="620px">
    <el-form label-width="88px">
      <el-form-item label="模板名称" required>
        <el-input v-model="form.templateName" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="模板编码" required>
        <el-input v-model="form.templateCode" placeholder="请输入模板编码" />
      </el-form-item>
      <el-form-item label="模板类型">
        <el-select v-model="form.templateType" style="width: 100%">
          <el-option label="验证码" :value="0" />
          <el-option label="通知类" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="模板内容">
        <el-input v-model="form.templateContent" type="textarea" :rows="5" placeholder="请输入模板内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="短信模板详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="模板名称">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="模板状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="模板编码">
        {{ currentRow.displayCode }}
      </el-descriptions-item>
      <el-descriptions-item label="模板内容">
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
