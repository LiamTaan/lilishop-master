<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  createParameter,
  deleteParameter,
  getParameterDetail,
  getParameterPage,
  updateParameter
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiPayload, extractApiRecords, formatAdminDateTime } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "ParameterManage"
});

const rows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const editingId = ref("");
const detailRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  id: "",
  paramName: "",
  options: "",
  isIndex: 1,
  required: 1,
  sort: 0,
  categoryParameterListText: "[]"
});

const columns: TableColumnList = [
  { label: "参数名称", prop: "paramName", minWidth: 150 },
  { label: "选项值", prop: "options", minWidth: 280, showOverflowTooltip: true },
  { label: "索引", prop: "isIndexLabel", minWidth: 100 },
  { label: "必填", prop: "requiredLabel", minWidth: 100 },
  { label: "排序", prop: "sort", minWidth: 100 },
  { label: "更新时间", prop: "updateTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 220, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item =>
    query.keyword ? String(item.paramName).includes(query.keyword) : true
  )
);

const summaryCards = computed(() => [
  { label: "参数总数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "索引参数",
    value: filteredRows.value.filter(item => Number(item.isIndex) === 1).length,
    accent: "green" as const,
    hint: "前台筛选可用"
  },
  {
    label: "必填参数",
    value: filteredRows.value.filter(item => Number(item.required) === 1).length,
    accent: "blue" as const,
    hint: "上架时要求填写"
  },
  { label: "维护动作", value: "增改删", accent: "purple" as const, hint: "已接入参数模板维护" }
]);

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.paramId,
    paramName: item.paramName || "-",
    options: item.options || "-",
    isIndex: Number(item.isIndex ?? 0),
    required: Number(item.required ?? 0),
    sort: Number(item.sort ?? 0),
    isIndexLabel: Number(item.isIndex ?? 0) === 1 ? "可索引" : "不索引",
    requiredLabel: Number(item.required ?? 0) === 1 ? "必填" : "非必填",
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getParameterPage({
      pageNumber: 1,
      pageSize: 200,
      paramName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("参数列表加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  loadData();
}

function resetForm() {
  editingId.value = "";
  form.id = "";
  form.paramName = "";
  form.options = "";
  form.isIndex = 1;
  form.required = 1;
  form.sort = 0;
  form.categoryParameterListText = "[]";
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getParameterDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    editingId.value = String(detail.id || row.id);
    form.id = editingId.value;
    form.paramName = detail.paramName || "";
    form.options = detail.options || "";
    form.isIndex = Number(detail.isIndex ?? 1);
    form.required = Number(detail.required ?? 1);
    form.sort = Number(detail.sort ?? 0);
    form.categoryParameterListText = JSON.stringify(
      detail.categoryParameterList || [],
      null,
      2
    );
    dialogVisible.value = true;
  } catch (_error) {
    message("参数详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getParameterDetail(String(row.id));
    detailRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("参数详情加载失败", { type: "error" });
  }
}

async function submitForm() {
  if (!form.paramName.trim()) {
    message("请输入参数名称", { type: "warning" });
    return;
  }
  if (!form.options.trim()) {
    message("请输入参数选项值", { type: "warning" });
    return;
  }
  let categoryParameterList: any[] = [];
  try {
    categoryParameterList = JSON.parse(form.categoryParameterListText || "[]");
  } catch (_error) {
    message("分类关联 JSON 解析失败，请检查格式", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...(editingId.value ? { id: editingId.value } : {}),
      paramName: form.paramName.trim(),
      options: form.options.trim(),
      isIndex: form.isIndex,
      required: form.required,
      sort: form.sort,
      categoryParameterList
    };
    if (editingId.value) {
      await updateParameter(payload);
      message("参数更新成功", { type: "success" });
    } else {
      await createParameter(payload);
      message("参数创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    resetForm();
    await loadData();
  } catch (_error) {
    message("参数保存失败，请检查字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除参数「${row.paramName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteParameter(String(row.id));
    message("参数删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("参数删除失败，请先确认是否有分类绑定", { type: "error" });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="参数管理"
    description="承接商品参数模板的列表、增删改和详情查看。"
    api-path="/manager/goods/parameters"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    keyword-label="参数名称"
    keyword-placeholder="请输入参数名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增参数</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑参数' : '新增参数'"
    width="680px"
  >
    <el-form label-width="96px">
      <el-form-item label="参数名称" required>
        <el-input v-model="form.paramName" maxlength="5" show-word-limit />
      </el-form-item>
      <el-form-item label="选项值" required>
        <el-input
          v-model="form.options"
          type="textarea"
          :rows="4"
          placeholder="多个选项建议用逗号分隔"
        />
      </el-form-item>
      <el-form-item label="是否索引">
        <el-radio-group v-model="form.isIndex">
          <el-radio :value="1">可索引</el-radio>
          <el-radio :value="0">不索引</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否必填">
        <el-radio-group v-model="form.required">
          <el-radio :value="1">必填</el-radio>
          <el-radio :value="0">非必填</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
      </el-form-item>
      <el-form-item label="分类关联">
        <el-input
          v-model="form.categoryParameterListText"
          type="textarea"
          :rows="6"
          placeholder="可选，传 GoodsParamsDTO.categoryParameterList 的 JSON 数组"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="参数详情" size="40%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="参数名称">
        {{ detailRow.paramName }}
      </el-descriptions-item>
      <el-descriptions-item label="选项值">
        {{ detailRow.options }}
      </el-descriptions-item>
      <el-descriptions-item label="索引状态">
        {{ detailRow.isIndexLabel }}
      </el-descriptions-item>
      <el-descriptions-item label="必填状态">
        {{ detailRow.requiredLabel }}
      </el-descriptions-item>
      <el-descriptions-item label="排序值">
        {{ detailRow.sort }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ detailRow.updateTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
