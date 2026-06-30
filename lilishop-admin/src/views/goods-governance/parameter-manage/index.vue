<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  createParameter,
  deleteParameter,
  getCategoryPage,
  getParameterDetail,
  getParameterPage,
  updateParameter
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "ParameterManage"
});

type CategoryTreeOption = {
  label: string;
  value: string;
  children?: CategoryTreeOption[];
};

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const categoryOptions = ref<CategoryTreeOption[]>([]);
const categoryNameMap = ref(new Map<string, string>());
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const categoryLoading = ref(false);
const editingId = ref("");
const detailRow = ref<Record<string, any> | null>(null);
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  id: "",
  paramName: "",
  optionList: [] as string[],
  isIndex: 1,
  required: 1,
  sort: 0,
  categoryIds: [] as string[]
});

const columns: TableColumnList = [
  { label: "参数名称", prop: "paramName", minWidth: 150 },
  {
    label: "选项值",
    prop: "options",
    minWidth: 280,
    showOverflowTooltip: true
  },
  { label: "选项数", prop: "optionCount", minWidth: 100 },
  { label: "索引", prop: "isIndexLabel", minWidth: 100 },
  { label: "必填", prop: "requiredLabel", minWidth: 100 },
  { label: "排序", prop: "sort", minWidth: 100 },
  { label: "更新时间", prop: "updateTime", minWidth: 180 },
  {
    label: "操作",
    prop: "operation",
    fixed: "right",
    width: 220,
    slot: "operation"
  }
];

const filteredRows = computed(() =>
  rows.value.filter(item =>
    query.keyword ? String(item.paramName).includes(query.keyword) : true
  )
);
const selectedIds = computed(() => [
  ...new Set(
    selectedRows.value.map(item => String(item.id || "")).filter(Boolean)
  )
]);

const summaryCards = computed(() => [
  {
    label: "参数总数",
    value: filteredRows.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "索引参数",
    value: filteredRows.value.filter(item => Number(item.isIndex) === 1).length,
    accent: "green" as const,
    hint: "前台筛选可用"
  },
  {
    label: "必填参数",
    value: filteredRows.value.filter(item => Number(item.required) === 1)
      .length,
    accent: "blue" as const,
    hint: "上架时要求填写"
  },
  {
    label: "维护方式",
    value: "选项 + 分类表单",
    accent: "purple" as const,
    hint: "不再手填 JSON 结构"
  }
]);

function splitOptions(value: unknown) {
  if (!value) return [];
  return Array.from(
    new Set(
      String(value)
        .split(/[,，]/)
        .map(item => item.trim())
        .filter(Boolean)
    )
  );
}

function joinOptions(value: string[]) {
  return Array.from(
    new Set(value.map(item => item.trim()).filter(Boolean))
  ).join(",");
}

function normalizeId(value: unknown) {
  if (value === undefined || value === null) return "";
  return String(value).trim();
}

function buildCategoryTreeOptions(
  list: Record<string, any>[],
  nameMap: Map<string, string>,
  parentPath = ""
): CategoryTreeOption[] {
  return list.map(item => {
    const id = normalizeId(item.id || item.categoryId);
    const name = String(item.name || item.categoryName || "-");
    const fullName = parentPath ? `${parentPath} / ${name}` : name;
    if (id) {
      nameMap.set(id, fullName);
    }
    const childrenSource =
      Array.isArray(item.children) && item.children.length
        ? item.children
        : Array.isArray(item.child)
          ? item.child
          : [];
    return {
      label: fullName,
      value: id,
      children: buildCategoryTreeOptions(childrenSource, nameMap, fullName)
    };
  });
}

function resolveCategoryNames(categoryIds: string[]) {
  return categoryIds.map(id => categoryNameMap.value.get(id) || id);
}

function normalizeDetailRow(item: Record<string, any>) {
  const normalized = normalizeRow(item);
  const categoryIds = Array.isArray(item?.categoryParameterList)
    ? item.categoryParameterList
        .map((entry: Record<string, any>) => normalizeId(entry.categoryId))
        .filter(Boolean)
    : [];
  return {
    ...normalized,
    categoryIds,
    categoryNames: resolveCategoryNames(categoryIds),
    optionList: splitOptions(item.options)
  };
}

function normalizeRow(item: Record<string, any>) {
  const optionList = splitOptions(item.options);
  return {
    ...item,
    id: item.id || item.paramId,
    paramName: item.paramName || "-",
    options: optionList.join("、") || "-",
    optionCount: optionList.length,
    isIndex: Number(item.isIndex ?? 0),
    required: Number(item.required ?? 0),
    sort: Number(item.sort ?? 0),
    isIndexLabel: Number(item.isIndex ?? 0) === 1 ? "可索引" : "不索引",
    requiredLabel: Number(item.required ?? 0) === 1 ? "必填" : "非必填",
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

async function loadCategories() {
  categoryLoading.value = true;
  try {
    const response = await getCategoryPage();
    const records = extractApiRecords<Record<string, any>>(response) || [];
    const nextNameMap = new Map<string, string>();
    categoryOptions.value = buildCategoryTreeOptions(records, nextNameMap);
    categoryNameMap.value = nextNameMap;
  } catch (_error) {
    categoryOptions.value = [];
    categoryNameMap.value = new Map();
    message("分类选项加载失败，参数保存时请先确认分类接口可用", {
      type: "warning"
    });
  } finally {
    categoryLoading.value = false;
  }
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

function handleSelectionChange(selection: Record<string, any>[]) {
  selectedRows.value = selection;
}

function resetForm() {
  editingId.value = "";
  form.id = "";
  form.paramName = "";
  form.optionList = [];
  form.isIndex = 1;
  form.required = 1;
  form.sort = 0;
  form.categoryIds = [];
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
    form.optionList = splitOptions(detail.options);
    form.isIndex = Number(detail.isIndex ?? 1);
    form.required = Number(detail.required ?? 1);
    form.sort = Number(detail.sort ?? 0);
    form.categoryIds = Array.isArray(detail.categoryParameterList)
      ? detail.categoryParameterList
          .map((item: Record<string, any>) => normalizeId(item.categoryId))
          .filter(Boolean)
      : [];
    dialogVisible.value = true;
  } catch (_error) {
    message("参数详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getParameterDetail(String(row.id));
    detailRow.value = normalizeDetailRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("参数详情加载失败", { type: "error" });
  }
}

async function submitForm() {
  const paramName = form.paramName.trim();
  const options = joinOptions(form.optionList);
  if (!paramName) {
    message("请输入参数名称", { type: "warning" });
    return;
  }
  if (!options) {
    message("请至少维护一个参数选项", { type: "warning" });
    return;
  }
  if (options.length > 255) {
    message("参数选项总长度不能超过 255 个字符", { type: "warning" });
    return;
  }

  saving.value = true;
  try {
    const payload = {
      ...(editingId.value ? { id: editingId.value } : {}),
      paramName,
      options,
      isIndex: form.isIndex,
      required: form.required,
      sort: form.sort,
      categoryParameterList: form.categoryIds.map(categoryId => ({
        categoryId,
        parameterId: editingId.value || undefined
      }))
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
  await ElMessageBox.confirm(
    `确认删除参数「${row.paramName}」吗？`,
    "删除确认",
    {
      type: "warning"
    }
  );
  try {
    await deleteParameter(String(row.id));
    message("参数删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("参数删除失败，请先确认是否有分类绑定", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) {
    message("请先选择要删除的参数", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认批量删除选中的 ${selectedRows.value.length} 个参数吗？`,
    "批量删除确认",
    {
      type: "warning"
    }
  );
  const results = await Promise.allSettled(
    selectedRows.value.map(row => deleteParameter(String(row.id)))
  );
  const successCount = results.filter(
    item => item.status === "fulfilled"
  ).length;
  const failCount = results.length - successCount;
  if (failCount > 0) {
    message(`${successCount} 个参数删除成功，${failCount} 个删除失败`, {
      type: successCount > 0 ? "warning" : "error"
    });
  } else {
    message("参数批量删除成功", { type: "success" });
  }
  await loadData();
}

function exportParameters() {
  if (!filteredRows.value.length) {
    message("暂无可导出的参数", { type: "warning" });
    return;
  }
  const worksheet = utils.json_to_sheet(
    filteredRows.value.map(item => ({
      参数名称: item.paramName,
      选项值: item.options,
      选项数: item.optionCount,
      索引: item.isIndexLabel,
      必填: item.requiredLabel,
      排序: item.sort,
      更新时间: item.updateTime
    }))
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "参数管理");
  writeFile(workbook, "参数管理.xlsx");
  message("参数导出成功", { type: "success" });
}

onMounted(() => {
  Promise.allSettled([loadCategories(), loadData()]);
});
</script>

<template>
  <WholesaleAdminPage
    title="参数管理"
    description="这里就是商品参数模板与分类绑定的入口：新增/编辑参数时，在“关联分类”里勾选要生效的商品分类。"
    api-path="/manager/goods/parameters"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :selectable="true"
    :show-status-filter="false"
    keyword-label="参数名称"
    keyword-placeholder="请输入参数名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增参数</el-button>
      <el-button :disabled="!filteredRows.length" @click="exportParameters"
        >导出</el-button
      >
      <el-button
        type="danger"
        :disabled="!selectedIds.length"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
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
    width="720px"
  >
    <el-form label-width="96px">
      <el-form-item label="参数名称" required>
        <el-input v-model="form.paramName" maxlength="5" show-word-limit />
      </el-form-item>
      <el-form-item label="选项值" required>
        <el-select
          v-model="form.optionList"
          multiple
          filterable
          allow-create
          default-first-option
          collapse-tags
          collapse-tags-tooltip
          :reserve-keyword="false"
          placeholder="输入后回车，可连续维护多个选项"
          style="width: 100%"
        >
          <el-option
            v-for="item in form.optionList"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        <div class="form-tip">
          系统仍会按后端契约保存为逗号分隔字符串，这里改成标签化录入，避免手工拼接。
        </div>
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
        <el-input-number
          v-model="form.sort"
          :min="0"
          :max="9999"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="关联分类">
        <el-tree-select
          v-model="form.categoryIds"
          v-loading="categoryLoading"
          :data="categoryOptions"
          node-key="value"
          multiple
          show-checkbox
          check-strictly
          filterable
          clearable
          :render-after-expand="false"
          placeholder="不选表示暂不绑定具体分类"
          style="width: 100%"
        />
        <div class="form-tip">
          后端实际接收的是
          `categoryParameterList`，这里按分类多选生成对应关联对象，不再暴露
          JSON。
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="参数详情" size="42%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="参数名称">
        {{ detailRow.paramName }}
      </el-descriptions-item>
      <el-descriptions-item label="选项值">
        <div class="detail-tag-list">
          <el-tag
            v-for="item in detailRow.optionList"
            :key="item"
            effect="plain"
          >
            {{ item }}
          </el-tag>
          <span v-if="!detailRow.optionList.length">-</span>
        </div>
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
      <el-descriptions-item label="关联分类">
        <div class="detail-tag-list">
          <el-tag
            v-for="item in detailRow.categoryNames"
            :key="item"
            type="success"
            effect="plain"
          >
            {{ item }}
          </el-tag>
          <span v-if="!detailRow.categoryNames.length">未绑定分类</span>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ detailRow.updateTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.form-tip {
  margin-top: 8px;
  color: #8a5b20;
  font-size: 12px;
  line-height: 1.5;
}

.detail-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
