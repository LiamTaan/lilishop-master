<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createSpecial,
  deleteSpecial,
  getSpecialPage,
  updateSpecial
} from "@/api/super-admin";
import {
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "ContentSpecialManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });
const form = reactive({
  specialName: "",
  clientType: "APP"
});

const columns: TableColumnList = [
  { label: "专题名称", prop: "displayName", minWidth: 220 },
  { label: "客户端", prop: "clientType", minWidth: 120 },
  { label: "页面ID", prop: "pageDataId", minWidth: 180, showOverflowTooltip: true },
  { label: "更新时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item =>
    query.keyword ? String(item.displayName || "").includes(query.keyword) : true
  )
);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const summaryCards = computed(() => [
  { label: "专题总数", value: filteredRows.value.length, accent: "orange" as const, hint: "内容与运营专题" },
  { label: "APP专题", value: filteredRows.value.filter(item => item.clientType === "APP").length, accent: "green" as const, hint: "首页可选专题源" },
  { label: "治理动作", value: "新增/编辑/删除/导出", accent: "blue" as const, hint: "专题独立维护" }
]);

function normalizeRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.specialId || item.pageDataId,
    displayName: item.specialName || item.name || "-",
    clientType: item.clientType || "APP",
    pageDataId: item.pageDataId || "-",
    displayTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

async function loadData() {
  try {
    rows.value = extractApiRecords(await getSpecialPage({ pageNumber: 1, pageSize: 200 }))
      .map(normalizeRecord);
  } catch (_error) {
    rows.value = [];
    message("专题加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  query.status = payload.status || "";
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  loadData();
}

function handleSelectionChange(list: Record<string, any>[]) {
  selectedRows.value = list;
}

function openCreate() {
  editingRow.value = null;
  form.specialName = "";
  form.clientType = "APP";
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.specialName = row.specialName || row.displayName || "";
  form.clientType = row.clientType || "APP";
  dialogVisible.value = true;
}

async function submit() {
  if (!form.specialName.trim()) {
    message("请输入专题名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      specialName: form.specialName.trim(),
      clientType: form.clientType
    };
    if (editingRow.value) {
      await updateSpecial(String(editingRow.value.id), payload);
      message("专题已更新", { type: "success" });
    } else {
      await createSpecial(payload);
      message("专题已新增", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("专题保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除专题「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  await deleteSpecial(String(row.id));
  message("专题已删除", { type: "success" });
  await loadData();
}

async function batchRemove() {
  if (!selectedIds.value.length) {
    message("请先勾选专题", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个专题吗？`, "批量删除", {
    type: "warning"
  });
  for (const id of selectedIds.value) {
    await deleteSpecial(id);
  }
  message("专题批量删除完成", { type: "success" });
  await loadData();
}

function exportRows() {
  if (!filteredRows.value.length) {
    message("暂无可导出的专题", { type: "warning" });
    return;
  }
  const workbook = utils.book_new();
  utils.book_append_sheet(
    workbook,
    utils.json_to_sheet(filteredRows.value.map(item => ({
      专题名称: item.displayName,
      客户端: item.clientType,
      页面ID: item.pageDataId,
      更新时间: item.displayTime
    }))),
    "专题管理"
  );
  writeFile(workbook, "专题管理.xlsx");
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="专题管理"
    description="首页专题、活动专题和内容专题统一在这里维护，首页只消费专题业务数据。"
    api-path="/manager/other/special"
    :columns="columns"
    :data="filteredRows"
    selectable
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '新增专题', value: '已接入', type: 'primary' },
      { label: '编辑删除', value: '已接入', type: 'success' },
      { label: '首页专题源', value: '业务归位', type: 'warning' }
    ]"
    keyword-label="专题名称"
    keyword-placeholder="请输入专题名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增专题</el-button>
      <el-button type="danger" plain @click="batchRemove">批量删除</el-button>
      <el-button @click="exportRows">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="remove(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑专题' : '新增专题'" width="520px">
    <el-form label-width="88px">
      <el-form-item label="专题名称" required>
        <el-input v-model="form.specialName" placeholder="请输入专题名称" />
      </el-form-item>
      <el-form-item label="客户端">
        <el-select v-model="form.clientType" style="width: 100%">
          <el-option label="APP" value="APP" />
          <el-option label="H5" value="H5" />
          <el-option label="PC" value="PC" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
