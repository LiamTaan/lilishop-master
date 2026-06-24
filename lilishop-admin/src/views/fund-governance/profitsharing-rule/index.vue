<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  createProfitSharingRule,
  deleteProfitSharingRule,
  getProfitSharingRulePage,
  updateProfitSharingRule
} from "@/api/fund-governance";
import { getCategoryPage } from "@/api/goods-governance";
import { getRegionAllCityTree } from "@/api/super-admin";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getEnableStatusLabel,
  getProfitSharingRoleLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "ProfitSharingRule"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const editingRow = ref<Record<string, any> | null>(null);
const detail = ref<Record<string, any>>({});
const regionOptions = ref<Record<string, any>[]>([]);
const categoryOptions = ref<Record<string, any>[]>([]);
const regionNameMap = ref(new Map<string, string>());
const categoryNameMap = ref(new Map<string, string>());
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  roleType: ""
});
const form = reactive({
  ruleName: "",
  roleType: "",
  regionId: "",
  categoryId: "",
  ratio: 0,
  status: "ENABLE"
});

const roleOptions = [
  { label: "代理商", value: "AGENT" },
  { label: "供货商", value: "STORE" },
  { label: "平台", value: "PLATFORM" },
  { label: "会员", value: "MEMBER" }
];

const scopeTips = [
  "角色类型决定分账对象。当前批发业务里优先维护“代理商”规则。",
  "区域留空表示全国通用，品类留空表示全品类通用。",
  "建议同时准备一条默认规则，再按区域或品类补更细规则，避免漏分账。",
  "当前后端现状是：规则表用于治理维护，分账明细仍按账单状态生成，尚未自动按此规则表计算实付分账。"
];

function normalizeId(value: unknown) {
  if (value === undefined || value === null) return "";
  const text = String(value).trim();
  if (!text || text === "-") return "";
  return text;
}

function resolveRegionName(regionId: string) {
  return regionId ? regionNameMap.value.get(regionId) || regionId : "";
}

function resolveCategoryName(categoryId: string) {
  return categoryId ? categoryNameMap.value.get(categoryId) || categoryId : "";
}

function formatScopeDisplay(name: string, id: string, fallback: string) {
  if (!id) return fallback;
  return name ? `${name}（${id}）` : id;
}

function normalizeRuleRecord(item: Record<string, any>) {
  const regionId = normalizeId(item.regionId);
  const categoryId = normalizeId(item.categoryId);
  const roleType = normalizeId(item.roleType || item.targetRoleType);
  return {
    ...item,
    id: item.id || item.ruleId,
    ruleName: item.ruleName || item.name || "-",
    roleType,
    roleTypeLabel: getProfitSharingRoleLabel(roleType),
    regionId,
    categoryId,
    regionName: item.regionName || item.regionPath || resolveRegionName(regionId),
    categoryName:
      item.categoryName ||
      item.goodsCategoryName ||
      resolveCategoryName(categoryId),
    ratio: Number(item.ratio ?? item.shareRatio ?? 0),
    status: item.status || item.enableStatus || "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.ruleName || "").includes(query.keyword)
      : true;
    const roleTypeMatched = extraFilters.roleType
      ? String(item.roleType) === extraFilters.roleType
      : true;
    const statusMatched = query.status ? String(item.status) === query.status : true;
    return keywordMatched && roleTypeMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  { label: "规则总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前分页结果" },
  {
    label: "启用规则",
    value: filteredData.value.filter(item => getEnableStatusLabel(item.status) === "启用").length,
    accent: "green" as const,
    hint: "当前有效分账规则"
  },
  {
    label: "角色维度",
    value: [...new Set(filteredData.value.map(item => item.roleTypeLabel))].length,
    accent: "blue" as const,
    hint: "规则覆盖角色类型"
  },
  { label: "分账治理", value: "已接入", accent: "purple" as const, hint: "规则列表页" }
]);

const detailItems = computed(() => [
  { label: "规则名称", value: detail.value.ruleName || "-" },
  {
    label: "角色类型",
    value: detail.value.roleType
      ? `${detail.value.roleTypeLabel || getProfitSharingRoleLabel(detail.value.roleType)}（${detail.value.roleType}）`
      : "-"
  },
  {
    label: "适用区域",
    value: formatScopeDisplay(
      detail.value.regionName || "",
      detail.value.regionId || "",
      "全部区域"
    )
  },
  {
    label: "适用品类",
    value: formatScopeDisplay(
      detail.value.categoryName || "",
      detail.value.categoryId || "",
      "全品类"
    )
  },
  { label: "分账比例", value: `${detail.value.ratio ?? 0}%` },
  { label: "状态", value: getEnableStatusLabel(detail.value.status) }
]);

function buildTreeOptions(
  list: Record<string, any>[],
  nameMap: Map<string, string>,
  parentPath = ""
) {
  return list.map(item => {
    const id = normalizeId(item.id || item.regionId || item.categoryId);
    const name = String(item.name || item.regionName || item.categoryName || "-");
    const fullName = parentPath ? `${parentPath} / ${name}` : name;
    if (id) {
      nameMap.set(id, fullName);
    }
    const childrenSource = Array.isArray(item.children) ? item.children : [];
    return {
      label: fullName,
      value: id,
      children: buildTreeOptions(childrenSource, nameMap, fullName)
    };
  });
}

async function loadOptions() {
  try {
    const [regionRes, categoryRes] = await Promise.all([
      getRegionAllCityTree(),
      getCategoryPage()
    ]);
    const nextRegionMap = new Map<string, string>();
    const nextCategoryMap = new Map<string, string>();
    const regionTree = extractApiPayload<Record<string, any>[]>(regionRes) || [];
    const categoryTree = extractApiRecords<Record<string, any>>(categoryRes) || [];
    regionOptions.value = buildTreeOptions(regionTree, nextRegionMap);
    categoryOptions.value = buildTreeOptions(categoryTree, nextCategoryMap);
    regionNameMap.value = nextRegionMap;
    categoryNameMap.value = nextCategoryMap;
    data.value = data.value.map(normalizeRuleRecord);
  } catch (_error) {
    regionOptions.value = [];
    categoryOptions.value = [];
    message("区域/品类选项加载失败，已保留规则基础查询", { type: "warning" });
  }
}

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 10
    };
    const res = await getProfitSharingRulePage(params);
    data.value = extractApiRecords(res).map(normalizeRuleRecord);
  } catch (_error) {
    data.value = [];
    message("分账规则加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.roleType = "";
  loadData();
}

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
}

function resetForm() {
  form.ruleName = "";
  form.roleType = "AGENT";
  form.regionId = "";
  form.categoryId = "";
  form.ratio = 0;
  form.status = "ENABLE";
}

function openCreate() {
  editingRow.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.ruleName = row.ruleName || "";
  form.roleType = row.roleType || "";
  form.regionId = row.regionId || "";
  form.categoryId = row.categoryId || "";
  form.ratio = Number(row.ratio || 0);
  form.status = row.status || "ENABLE";
  dialogVisible.value = true;
}

async function handleSave() {
  if (!form.ruleName.trim() || !form.roleType.trim()) {
    message("规则名称和角色类型不能为空", { type: "warning" });
    return;
  }
  if (Number(form.ratio) < 0 || Number(form.ratio) > 100) {
    message("分账比例请填写 0 到 100 之间的数值", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ruleName: form.ruleName.trim(),
      roleType: form.roleType.trim(),
      regionId: normalizeId(form.regionId) || undefined,
      categoryId: normalizeId(form.categoryId) || undefined,
      ratio: Number(form.ratio || 0),
      status: form.status
    };
    if (editingRow.value) {
      await updateProfitSharingRule(String(editingRow.value.id), payload);
      message("分账规则修改成功", { type: "success" });
    } else {
      await createProfitSharingRule(payload);
      message("分账规则新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("分账规则保存失败，请确认字段是否正确", {
      type: "error"
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteProfitSharingRule(String(row.id));
    message("分账规则删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("分账规则删除失败，请稍后重试", { type: "error" });
  }
}

function exportRules() {
  if (!filteredData.value.length) {
    message("暂无可导出的分账规则", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    规则名称: item.ruleName,
    角色类型: `${item.roleTypeLabel || getProfitSharingRoleLabel(item.roleType)} (${item.roleType || "-"})`,
    适用区域: formatScopeDisplay(item.regionName || "", item.regionId || "", "全部区域"),
    适用品类: formatScopeDisplay(item.categoryName || "", item.categoryId || "", "全品类"),
    分账比例: `${item.ratio ?? 0}%`,
    状态: getEnableStatusLabel(item.status)
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "分账规则");
  writeFile(workbook, "分账规则.xlsx");
  message("分账规则导出成功", { type: "success" });
}

onMounted(() => {
  Promise.allSettled([loadOptions(), loadData()]);
});
</script>

<template>
  <el-alert
    type="info"
    show-icon
    :closable="false"
    class="rule-guide"
  >
    <template #title>分账规则维护说明</template>
    <div class="rule-guide__content">
      <div v-for="tip in scopeTips" :key="tip">{{ tip }}</div>
    </div>
  </el-alert>

  <WholesaleAdminPage
    title="分账规则"
    description="承接平台分账规则查询、新增、编辑、删除与导出治理。"
    api-path="/manager/profitsharing/rule"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '启用', value: 'ENABLE' },
      { label: '停用', value: 'DISABLE' }
    ]"
    :quick-actions="[
      { label: '规则治理', value: '新增/编辑/删除', type: 'warning' },
      { label: '角色筛选', value: '已改为选择', type: 'success' },
      { label: '比例导出', value: '已接入', type: 'primary' }
    ]"
    keyword-label="规则名称"
    keyword-placeholder="请输入分账规则名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增规则</el-button>
      <el-button @click="exportRules">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="角色类型">
        <el-select
          v-model="extraFilters.roleType"
          placeholder="请选择角色类型"
          clearable
          style="width: 220px"
        >
          <el-option
            v-for="option in roleOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="分账规则详情" size="42%">
    <el-descriptions :column="1" border>
      <el-descriptions-item
        v-for="item in detailItems"
        :key="item.label"
        :label="item.label"
      >
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑分账规则' : '新增分账规则'" width="560px">
    <el-form label-width="96px">
      <el-form-item label="规则名称" required>
        <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
      </el-form-item>
      <el-form-item label="角色类型" required>
        <el-select
          v-model="form.roleType"
          placeholder="请选择角色类型"
          style="width: 100%"
        >
          <el-option
            v-for="option in roleOptions"
            :key="option.value"
            :label="`${option.label} (${option.value})`"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="适用区域">
        <el-tree-select
          v-model="form.regionId"
          :data="regionOptions"
          node-key="value"
          check-strictly
          filterable
          clearable
          default-expand-all
          :render-after-expand="false"
          placeholder="不选则默认全部区域"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="适用品类">
        <el-tree-select
          v-model="form.categoryId"
          :data="categoryOptions"
          node-key="value"
          check-strictly
          filterable
          clearable
          :render-after-expand="false"
          placeholder="不选则默认全品类"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="分账比例">
        <el-input-number
          v-model="form.ratio"
          :min="0"
          :max="100"
          :precision="2"
          style="width: 100%"
        />
      </el-form-item>
      <div class="form-tip">
        维护建议：先建一条默认规则，再按区域或品类增加更细规则，比例单位为百分比。
      </div>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="启用" value="ENABLE" />
          <el-option label="停用" value="DISABLE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.rule-guide {
  margin-bottom: 16px;
}

.rule-guide__content {
  display: grid;
  gap: 6px;
  line-height: 1.5;
}

.form-tip {
  margin: -4px 0 14px 96px;
  font-size: 12px;
  color: #8a5b20;
  line-height: 1.5;
}
</style>
