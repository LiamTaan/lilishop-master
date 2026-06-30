<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createSmsSign,
  deleteSmsSign,
  getSmsSignDetail,
  getSmsSignPage,
  modifySmsSign,
  querySmsSignStatus
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "SmsSignManage"
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
  signName: "",
  remark: "",
  signType: 0
});

const columns: TableColumnList = [
  { label: "签名名称", prop: "displayName", minWidth: 220 },
  { label: "审核状态", prop: "displayStatus", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "备注说明", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  {
    label: "签名总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前短信签名总量"
  },
  {
    label: "审核通过",
    value: rows.value.filter(item => String(item.displayStatus).includes("SUCCESS")).length,
    accent: "green" as const,
    hint: "已通过审核"
  },
  {
    label: "待审核",
    value: rows.value.filter(item => String(item.displayStatus).includes("WAIT")).length,
    accent: "blue" as const,
    hint: "待平台查询状态"
  },
  {
    label: "治理动作",
    value: "新增/修改/删除",
    accent: "purple" as const,
    hint: "承接真实签名接口"
  }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.signId || item.signName,
    displayName: item.signName || item.name || "-",
    displayStatus: item.signStatus || item.status || item.auditStatus || "UNKNOWN",
    displayTime: item.updateTime || item.createTime || "-",
    displayRemark: item.remark || item.reason || "短信签名治理记录"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.status) params.signStatus = Number(query.status);
    const res = await getSmsSignPage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(item => item.displayName.includes(query.keyword));
    }
    rows.value = list;
  } catch (_error) {
    message("短信签名加载失败，请稍后重试", { type: "error" });
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
  form.signName = "";
  form.remark = "";
  form.signType = 0;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getSmsSignDetail(String(row.id));
    const detail = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
    form.signName = detail.signName || detail.displayName;
    form.remark = detail.remark || detail.displayRemark;
    form.signType = Number(detail.signType || 0);
  } catch (_error) {
    form.signName = row.signName || row.displayName;
    form.remark = row.remark || row.displayRemark;
    form.signType = Number(row.signType || 0);
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.signName.trim()) {
    message("请输入签名名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      id: editingRow.value?.id,
      signName: form.signName,
      remark: form.remark,
      signType: form.signType
    };
    if (editingRow.value) {
      await modifySmsSign(payload);
      message("短信签名修改成功", { type: "success" });
    } else {
      await createSmsSign(payload);
      message("短信签名新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("短信签名保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除短信签名「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteSmsSign(String(row.id));
    message("短信签名删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信签名删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的短信签名", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个短信签名吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteSmsSign(id)));
    selectedRows.value = [];
    message("短信签名批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信签名批量删除失败", { type: "error" });
  }
}

async function handleQueryStatus() {
  try {
    await querySmsSignStatus();
    message("短信签名状态查询已触发", { type: "success" });
    await loadData();
  } catch (_error) {
    message("短信签名状态查询失败", { type: "error" });
  }
}

function exportSmsSigns() {
  if (!rows.value.length) {
    message("暂无可导出的短信签名数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    签名名称: item.displayName,
    审核状态: item.displayStatus,
    更新时间: item.displayTime,
    备注说明: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "短信签名");
  writeFile(workbook, "短信签名.xlsx");
  message("短信签名导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="短信签名"
    description="承接短信签名申请、签名状态查询、修改和删除动作，作为通知与消息侧的真实配置页。"
    api-path="/manager/sms/sign/querySmsSignPage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '新增签名', value: '已接入', type: 'primary' },
      { label: '状态查询', value: '已接入', type: 'warning' },
      { label: '修改删除', value: '已接入', type: 'success' }
    ]"
    keyword-label="签名名称"
    keyword-placeholder="请输入签名名称"
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
      <el-button @click="exportSmsSigns">导出</el-button>
      <el-button plain @click="handleQueryStatus">查询签名状态</el-button>
      <el-button type="primary" @click="openCreate">新增签名</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">修改</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '修改短信签名' : '新增短信签名'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="签名名称" required>
        <el-input v-model="form.signName" placeholder="请输入签名名称" />
      </el-form-item>
      <el-form-item label="签名类型">
        <el-select v-model="form.signType" style="width: 100%">
          <el-option label="验证码" :value="0" />
          <el-option label="营销通知" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注说明">
        <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入备注说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="短信签名详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="签名名称">
        {{ currentRow.displayName }}
      </el-descriptions-item>
      <el-descriptions-item label="审核状态">
        {{ currentRow.displayStatus }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ currentRow.displayTime }}
      </el-descriptions-item>
      <el-descriptions-item label="备注说明">
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
