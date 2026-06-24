<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createVerificationSource,
  deleteVerificationSource,
  getVerificationSourceDetail,
  getVerificationSourcePage,
  updateVerificationSource
} from "@/api/super-admin";
import {
  extractApiRecords,
  formatAdminDateTime,
  getVerificationSourceTypeLabel,
  normalizeVerificationSourceType
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({ type: "", resource: "", description: "" });

const columns: TableColumnList = [
  { label: "资源类型", prop: "displayType", minWidth: 160 },
  { label: "资源标识", prop: "displayName", minWidth: 220 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "说明", prop: "displayRemark", minWidth: 220, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "资源总数", value: rows.value.length, accent: "orange" as const, hint: "当前验证码资源台账" },
  { label: "资源完整", value: rows.value.filter(item => item.displayName !== "-").length, accent: "green" as const, hint: "已配置资源标识" },
  { label: "待补说明", value: rows.value.filter(item => !item.displayRemark || item.displayRemark === "-").length, accent: "blue" as const, hint: "待完善资源说明" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接滑块验证码资源维护接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.resource || item.type,
    normalizedType: normalizeVerificationSourceType(item.type),
    displayType: getVerificationSourceTypeLabel(item.type || "-"),
    displayName: item.resource || item.name || "-",
    displayTime: formatAdminDateTime(item.updateTime || item.createTime || "-"),
    displayRemark: item.description || item.remark || "-"
  };
}

async function loadData() {
  try {
    const res = await getVerificationSourcePage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) {
      list = list.filter(
        item =>
          item.displayName.includes(query.keyword) ||
          item.displayType.includes(query.keyword)
      );
    }
    rows.value = list;
  } catch (_error) {
    message("验证码资源加载失败，请稍后重试", { type: "error" });
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

function openCreate() {
  editingRow.value = null;
  form.type = "";
  form.resource = "";
  form.description = "";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getVerificationSourceDetail(String(row.id));
    const detail = normalizeRecord(((res as any).result || (res as any).data || row) as Record<string, any>);
    form.type = detail.normalizedType || normalizeVerificationSourceType(detail.type);
    form.resource = detail.resource || detail.displayName;
    form.description = detail.description || detail.displayRemark;
  } catch (_error) {
    form.type = row.normalizedType || normalizeVerificationSourceType(row.type);
    form.resource = row.resource || row.displayName;
    form.description = row.description || row.displayRemark;
  }
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.type.trim() || !form.resource.trim()) {
    message("请输入资源类型和资源标识", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { type: form.type, resource: form.resource, description: form.description };
    if (editingRow.value) {
      await updateVerificationSource(String(editingRow.value.id), payload);
      message("验证码资源修改成功", { type: "success" });
    } else {
      await createVerificationSource(payload);
      message("验证码资源新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("验证码资源保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除资源「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteVerificationSource([String(row.id)]);
    message("验证码资源删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("验证码资源删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="验证码资源"
    description="维护滑块验证码的底图与滑块模板资源，服务登录与安全校验链路，不再与订单核销规则混用。"
    api-path="/manager/other/verificationSource"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '资源新增', value: '已接入', type: 'primary' },
      { label: '资源编辑', value: '已接入', type: 'success' },
      { label: '资源删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="资源类型/标识"
    keyword-placeholder="请输入资源类型或资源标识"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增资源</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑验证码资源' : '新增验证码资源'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="资源类型" required>
        <el-select v-model="form.type" placeholder="请选择资源类型" style="width: 100%">
          <el-option label="验证码源" value="RESOURCE" />
          <el-option label="滑块" value="SLIDER" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源标识" required><el-input v-model="form.resource" placeholder="请输入资源标识" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入说明" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="验证码资源详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="资源类型">{{ currentRow.displayType }}</el-descriptions-item>
      <el-descriptions-item label="资源标识">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="说明">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
