<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  createBrand,
  deleteBrands,
  getBrandDetail,
  getBrandPage,
  updateBrand,
  updateBrandDisable
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getEnableStatusLabel,
  getEnableStatusType
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "BrandManage"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailRow = ref<Record<string, any> | null>(null);
const dialogVisible = ref(false);
const saving = ref(false);
const editingId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  name: "",
  logo: ""
});

const columns: TableColumnList = [
  { label: "品牌名称", prop: "name", minWidth: 180 },
  { label: "品牌图标", prop: "logo", minWidth: 260, showOverflowTooltip: true },
  { label: "状态", prop: "statusLabel", minWidth: 120 },
  { label: "更新时间", prop: "updateTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 260, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.name).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.disable) === String(query.status === "DISABLE")
      : true;
    return keywordMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  {
    label: "品牌总数",
    value: filteredRows.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "启用品牌",
    value: filteredRows.value.filter(item => !item.disable).length,
    accent: "green" as const,
    hint: "前台可用品牌"
  },
  {
    label: "停用品牌",
    value: filteredRows.value.filter(item => item.disable).length,
    accent: "blue" as const,
    hint: "已停用品牌"
  },
  {
    label: "维护动作",
    value: "增改删/启停",
    accent: "purple" as const,
    hint: "已接入真实接口"
  }
]);

function normalizeRow(item: Record<string, any>) {
  const disable = Boolean(item.disable);
  return {
    ...item,
    id: item.id || item.brandId,
    name: item.name || "-",
    logo: item.logo || "-",
    disable,
    statusLabel: getEnableStatusLabel(disable ? "DISABLE" : "ENABLE"),
    statusType: getEnableStatusType(disable ? "DISABLE" : "ENABLE"),
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getBrandPage({
      pageNumber: 1,
      pageSize: 200,
      name: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("品牌列表加载失败，请稍后重试", { type: "error" });
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

function resetForm() {
  editingId.value = "";
  form.name = "";
  form.logo = "";
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getBrandDetail(String(row.id));
    const detail = normalizeRow(extractApiPayload(response) || row);
    editingId.value = String(detail.id);
    form.name = detail.name || "";
    form.logo = detail.logo || "";
    dialogVisible.value = true;
  } catch (_error) {
    message("品牌详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getBrandDetail(String(row.id));
    detailRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("品牌详情加载失败", { type: "error" });
  }
}

async function submitForm() {
  if (!form.name.trim()) {
    message("请输入品牌名称", { type: "warning" });
    return;
  }
  if (!form.logo.trim()) {
    message("请输入品牌图标地址", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: form.name.trim(),
      logo: form.logo.trim()
    };
    if (editingId.value) {
      await updateBrand(editingId.value, payload);
      message("品牌更新成功", { type: "success" });
    } else {
      await createBrand(payload);
      message("品牌创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    resetForm();
    await loadData();
  } catch (_error) {
    message("品牌保存失败，请检查字段后重试", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row: Record<string, any>) {
  const nextDisable = !row.disable;
  await ElMessageBox.confirm(
    `确认${nextDisable ? "停用" : "启用"}品牌「${row.name}」吗？`,
    "状态确认",
    { type: nextDisable ? "warning" : "success" }
  );
  try {
    await updateBrandDisable(String(row.id), nextDisable);
    message(`品牌已${nextDisable ? "停用" : "启用"}`, { type: "success" });
    await loadData();
  } catch (_error) {
    message("品牌状态更新失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除品牌「${row.name}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteBrands([String(row.id)]);
    message("品牌删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("品牌删除失败，请先确认是否存在关联数据", {
      type: "error"
    });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="品牌管理"
    description="承接品牌列表、启停和基础维护，作为商品治理的品牌 CRUD 入口。"
    api-path="/manager/goods/brand/getByPage"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '启用', value: 'ENABLE' },
      { label: '停用', value: 'DISABLE' }
    ]"
    keyword-label="品牌名称"
    keyword-placeholder="请输入品牌名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增品牌</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link :type="row.disable ? 'success' : 'warning'" @click="toggleStatus(row)">
        {{ row.disable ? "启用" : "停用" }}
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑品牌' : '新增品牌'"
    width="520px"
  >
    <el-form label-width="88px">
      <el-form-item label="品牌名称" required>
        <el-input v-model="form.name" maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item label="品牌图标" required>
        <el-input v-model="form.logo" placeholder="请输入品牌图标 URL" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="品牌详情" size="38%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="品牌名称">
        {{ detailRow.name }}
      </el-descriptions-item>
      <el-descriptions-item label="品牌图标">
        {{ detailRow.logo }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="detailRow.statusType" effect="light" round>
          {{ detailRow.statusLabel }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ detailRow.updateTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
