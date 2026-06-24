<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createStockReason,
  deleteStockReason,
  getStockReasonPage,
  updateStockReason
} from "@/api/procurement-governance";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns, getReasonCategoryLabel } from "./columns";

defineOptions({
  name: "ProcurementGovernanceReason"
});

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  category: ""
});
const form = reactive({
  id: "",
  reason: "",
  category: "INBOUND"
});

const summaryCards = computed(() => [
  {
    label: "原因总数",
    value: rows.value.length,
    accent: "orange" as const,
    hint: "当前库存原因条目数"
  },
  {
    label: "入库原因",
    value: rows.value.filter(item => item.category === "INBOUND").length,
    accent: "green" as const,
    hint: "入库场景原因"
  },
  {
    label: "报损/盘点",
    value: rows.value.filter(item =>
      ["DAMAGE", "COUNT"].includes(item.category)
    ).length,
    accent: "blue" as const,
    hint: "治理场景原因"
  },
  {
    label: "增改删",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持弹窗增改删"
  }
]);

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.reason,
    reason: item.reason || "-",
    category: item.category || "-",
    updateTime: item.updateTime || item.createTime
  };
}

async function loadData() {
  try {
    const res = await getStockReasonPage({
      pageNumber: 1,
      pageSize: 200,
      reason: query.keyword || undefined,
      category: query.category || undefined
    });
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("库存原因加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string }) {
  query.keyword = payload.keyword || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.category = "";
  loadData();
}

function openCreate() {
  editingRow.value = null;
  form.id = "";
  form.reason = "";
  form.category = query.category || "INBOUND";
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.id = String(row.id || "");
  form.reason = row.reason || "";
  form.category = row.category || "INBOUND";
  dialogVisible.value = true;
}

async function handleSave() {
  if (!form.reason.trim()) {
    message("请输入库存原因", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      id: editingRow.value ? form.id : undefined,
      reason: form.reason.trim(),
      category: form.category
    };
    if (editingRow.value) {
      await updateStockReason(payload);
      message("库存原因修改成功", { type: "success" });
    } else {
      await createStockReason(payload);
      message("库存原因新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("库存原因保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认删除库存原因「${row.reason}」吗？`,
    "删除确认",
    {
      type: "warning"
    }
  );
  try {
    await deleteStockReason(String(row.id));
    message("库存原因删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("库存原因删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="库存原因管理"
    description="承接管理端库存原因搜索、类别筛选与弹窗增改删，只对接 /manager/procurement/reason。"
    api-path="/manager/procurement/reason"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '原因新增', value: '已接入', type: 'primary' },
      { label: '原因编辑', value: '已接入', type: 'success' },
      { label: '原因删除', value: '已接入', type: 'warning' }
    ]"
    keyword-label="原因名称"
    keyword-placeholder="请输入库存原因"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="原因类别">
        <el-select
          v-model="query.category"
          style="width: 180px"
          clearable
          placeholder="全部类别"
          @change="loadData"
        >
          <el-option label="入库" value="INBOUND" />
          <el-option label="出库" value="OUTBOUND" />
          <el-option label="报损" value="DAMAGE" />
          <el-option label="盘点" value="COUNT" />
        </el-select>
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增库存原因</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingRow ? '编辑库存原因' : '新增库存原因'"
    width="560px"
  >
    <el-form label-width="88px">
      <el-form-item label="原因名称" required>
        <el-input v-model="form.reason" placeholder="请输入库存原因" />
      </el-form-item>
      <el-form-item label="原因类别">
        <el-select v-model="form.category" style="width: 100%">
          <el-option label="入库" value="INBOUND" />
          <el-option label="出库" value="OUTBOUND" />
          <el-option label="报损" value="DAMAGE" />
          <el-option label="盘点" value="COUNT" />
        </el-select>
      </el-form-item>
      <el-form-item label="预览">
        <el-text type="info">{{
          getReasonCategoryLabel(form.category)
        }}</el-text>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>
