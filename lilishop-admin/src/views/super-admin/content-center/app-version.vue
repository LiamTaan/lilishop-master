<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createAppVersion,
  deleteAppVersion,
  getAppVersionPage,
  updateAppVersion
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", type: "ANDROID" });
const form = reactive({
  version: "",
  type: "ANDROID",
  content: "",
  forceUpdate: false
});

const columns: TableColumnList = [
  { label: "版本号", prop: "displayName", minWidth: 180 },
  { label: "客户端", prop: "displayType", minWidth: 120 },
  { label: "强制更新", prop: "displayForce", minWidth: 120 },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 200, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "版本总数", value: rows.value.length, accent: "orange" as const, hint: "当前 APP 版本台账" },
  { label: "Android", value: rows.value.filter(item => item.displayType === "ANDROID").length, accent: "green" as const, hint: "Android 版本数" },
  { label: "iOS", value: rows.value.filter(item => item.displayType === "IOS").length, accent: "blue" as const, hint: "iOS 版本数" },
  { label: "治理动作", value: "新增/编辑/删除", accent: "purple" as const, hint: "承接真实版本接口" }
]);

function normalizeRecord(item: Record<string, any>): Record<string, any> {
  return {
    ...item,
    id: item.id || item.versionId || item.version,
    displayName: item.version || item.versionName || "-",
    displayType: item.type || "-",
    displayForce: item.forceUpdate ? "强制" : "可选",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.remark || item.content || "-"
  };
}

async function loadData() {
  try {
    const res = await getAppVersionPage({ type: query.type });
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    rows.value = list;
  } catch (_error) {
    message("APP版本加载失败，请稍后重试", { type: "error" });
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
  form.version = "";
  form.type = query.type;
  form.content = "";
  form.forceUpdate = false;
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.version = row.version || row.displayName;
  form.type = row.type || row.displayType || query.type;
  form.content = row.content || row.displayRemark;
  form.forceUpdate = Boolean(row.forceUpdate);
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.version.trim()) {
    message("请输入版本号", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      version: form.version,
      type: form.type,
      content: form.content,
      forceUpdate: form.forceUpdate
    };
    if (editingRow.value) {
      await updateAppVersion(String(editingRow.value.id), payload);
      message("APP版本修改成功", { type: "success" });
    } else {
      await createAppVersion(payload);
      message("APP版本新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("APP版本保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除版本「${row.displayName}」吗？`, "删除确认", { type: "warning" });
  try {
    await deleteAppVersion(String(row.id));
    message("APP版本删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("APP版本删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="APP版本"
    description="承接 APP 版本台账、版本维护和版本删除动作，作为内容运营中的版本治理页。"
    api-path="/manager/other/appVersion"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '版本新增', value: '已接入', type: 'primary' },
      { label: '版本编辑', value: '已接入', type: 'success' },
      { label: '版本删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="版本号"
    keyword-placeholder="请输入版本号"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="客户端">
        <el-select v-model="query.type" style="width: 160px" @change="loadData">
          <el-option label="ANDROID" value="ANDROID" />
          <el-option label="IOS" value="IOS" />
        </el-select>
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增版本</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑 APP 版本' : '新增 APP 版本'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="版本号" required><el-input v-model="form.version" placeholder="请输入版本号" /></el-form-item>
      <el-form-item label="客户端">
        <el-select v-model="form.type" style="width: 100%">
          <el-option label="ANDROID" value="ANDROID" />
          <el-option label="IOS" value="IOS" />
        </el-select>
      </el-form-item>
      <el-form-item label="强制更新"><el-switch v-model="form.forceUpdate" /></el-form-item>
      <el-form-item label="升级内容"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入升级内容" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="APP版本详情" size="560px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="版本号">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="客户端">{{ currentRow.displayType }}</el-descriptions-item>
      <el-descriptions-item label="强制更新">{{ currentRow.displayForce }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.displayTime }}</el-descriptions-item>
      <el-descriptions-item label="升级内容">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; color: #5d6168; }
</style>
