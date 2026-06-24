<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  createCategory,
  deleteCategory,
  getBrandOptions,
  getCategoryBrandCategories,
  getCategoryPage,
  saveCategoryBrandCategories,
  updateCategory
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiRecords, formatAdminDateTime } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "CategoryManage"
});

const data = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const detailLoading = ref(false);
const saving = ref(false);
const brandLoading = ref(false);
const brandSaving = ref(false);
const editingRow = ref<Record<string, any> | null>(null);
const detail = ref<Record<string, any>>({});
const detailActiveTab = ref("basic");
const brandOptions = ref<Record<string, any>[]>([]);
const selectedBrandIds = ref<string[]>([]);
const brandCategorySnapshot = ref<Record<string, string[]>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  levelKeyword: ""
});
const form = reactive({
  name: "",
  parentId: "0",
  level: 0,
  sortOrder: 0,
  commissionRate: 0,
  image: "",
  supportChannel: false
});

function flattenTree(
  list: Record<string, any>[],
  bucket: Record<string, any>[] = []
) {
  list.forEach(item => {
    bucket.push(item);
    const children = item.children || item.child || [];
    if (Array.isArray(children) && children.length) {
      flattenTree(children, bucket);
    }
  });
  return bucket;
}

function normalizeCategoryRecord(item: Record<string, any>) {
  const level =
    item.level ?? item.grade ?? item.categoryLevel ?? item.catLevel ?? "-";
  const deleteFlag = Boolean(item.deleteFlag);
  return {
    ...item,
    id: item.id || item.categoryId,
    categoryName: item.name || item.categoryName || "-",
    parentId: item.parentId ?? "0",
    parentTitle: item.parentTitle || item.parentName || "-",
    levelValue: typeof level === "number" ? level : Number(level) || 0,
    level:
      typeof level === "number"
        ? `${level}级`
        : `${String(level).replace("LEVEL_", "") || 0}级`,
    sortOrder: Number(item.sortOrder ?? item.sort ?? item.categorySort ?? 0),
    commissionRate: Number(item.commissionRate ?? 0),
    supportChannel: Boolean(item.supportChannel),
    status: deleteFlag ? "DISABLE" : "ENABLE",
    statusLabel: deleteFlag ? "停用" : "启用",
    createTime: item.createTime || item.createDate || "-",
    updateTime: item.updateTime || item.createTime || item.createDate || "-"
  };
}

function normalizeBrandOption(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.brandId,
    name: item.name || item.brandName || "-",
    logo: item.logo || ""
  };
}

const filteredData = computed(() =>
  data.value.filter(item => {
    const levelMatched = extraFilters.levelKeyword
      ? String(item.level).includes(extraFilters.levelKeyword)
      : true;
    const statusMatched = query.status ? item.status === query.status : true;
    const keywordMatched = query.keyword
      ? String(item.categoryName).includes(query.keyword)
      : true;
    return levelMatched && statusMatched && keywordMatched;
  })
);

const summaryCards = computed(() => [
  {
    label: "分类总数",
    value: filteredData.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "一级分类",
    value: filteredData.value.filter(item => item.levelValue === 0).length,
    accent: "blue" as const,
    hint: "顶级导航分类"
  },
  {
    label: "启用分类",
    value: filteredData.value.filter(item => item.status === "ENABLE").length,
    accent: "green" as const,
    hint: "当前前台可见分类"
  },
  {
    label: "维护动作",
    value: "新增/编辑/删除/导出",
    accent: "purple" as const,
    hint: "已接入真实分类维护"
  }
]);

const detailItems = computed(() => [
  { label: "分类名称", value: detail.value.categoryName || "-" },
  { label: "上级分类ID", value: detail.value.parentId || "0" },
  { label: "分类层级", value: detail.value.level || "-" },
  { label: "排序值", value: detail.value.sortOrder ?? 0 },
  { label: "佣金比例", value: detail.value.commissionRate ?? 0 },
  { label: "支持频道", value: detail.value.supportChannel ? "支持" : "不支持" },
  { label: "状态", value: detail.value.statusLabel || "-" },
  {
    label: "更新时间",
    value: formatAdminDateTime(detail.value.updateTime || detail.value.createTime)
  }
]);

const selectedBrandCount = computed(() => selectedBrandIds.value.length);

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.name = query.keyword;
    const res = await getCategoryPage(params);
    const payload = extractApiRecords(res);
    const source = Array.isArray(payload) ? payload : [];
    data.value = flattenTree(source).map(normalizeCategoryRecord);
  } catch (_error) {
    data.value = [];
    message("分类列表加载失败，请稍后重试", { type: "error" });
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
  extraFilters.levelKeyword = "";
  loadData();
}

function resetForm() {
  form.name = "";
  form.parentId = "0";
  form.level = 0;
  form.sortOrder = 0;
  form.commissionRate = 0;
  form.image = "";
  form.supportChannel = false;
}

function openCreate() {
  editingRow.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.name = row.name || row.categoryName || "";
  form.parentId = row.parentId || "0";
  form.level = Number(row.levelValue ?? row.level ?? 0);
  form.sortOrder = Number(row.sortOrder ?? 0);
  form.commissionRate = Number(row.commissionRate ?? 0);
  form.image = row.image || "";
  form.supportChannel = Boolean(row.supportChannel);
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
  detailActiveTab.value = "basic";
  loadCategoryBrandBindings(String(row.id));
}

async function loadCategoryBrandBindings(categoryId: string) {
  detailLoading.value = true;
  brandLoading.value = true;
  try {
    const optionsResponse = await getBrandOptions();
    const options = extractApiRecords(optionsResponse).map(normalizeBrandOption);
    brandOptions.value = options;

    const snapshotEntries = await Promise.all(
      options.map(async option => {
        const brandId = String(option.id);
        const categoriesResponse = await getCategoryBrandCategories(brandId);
        const categoryIds = extractApiRecords(categoriesResponse).map(category =>
          String(category.id || category.categoryId)
        );
        return [brandId, categoryIds] as const;
      })
    );

    const snapshot = Object.fromEntries(snapshotEntries);
    brandCategorySnapshot.value = snapshot;
    selectedBrandIds.value = options
      .filter(option => snapshot[String(option.id)]?.includes(categoryId))
      .map(option => String(option.id));
  } catch (_error) {
    brandOptions.value = [];
    selectedBrandIds.value = [];
    brandCategorySnapshot.value = {};
    message("分类关联品牌加载失败，请确认品牌关联接口可用", {
      type: "error"
    });
  } finally {
    detailLoading.value = false;
    brandLoading.value = false;
  }
}

async function handleSave() {
  if (!form.name.trim()) {
    message("请输入分类名称", { type: "warning" });
    return;
  }
  if (!form.parentId.trim()) {
    message("请输入上级分类ID，顶级请填写 0", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...(editingRow.value ? { id: editingRow.value.id } : {}),
      name: form.name.trim(),
      parentId: form.parentId.trim(),
      level: form.level,
      sortOrder: form.sortOrder,
      commissionRate: form.commissionRate,
      image: form.image || undefined,
      supportChannel: form.supportChannel
    };
    if (editingRow.value) {
      await updateCategory(payload);
      message("分类修改成功", { type: "success" });
    } else {
      await createCategory(payload);
      message("分类新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("分类保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认删除分类「${row.categoryName}」吗？`,
    "删除确认",
    {
      type: "warning"
    }
  );
  try {
    await deleteCategory(String(row.id));
    message("分类删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("分类删除失败，请确认是否存在子分类或关联商品", {
      type: "error"
    });
  }
}

function exportCategory() {
  if (!filteredData.value.length) {
    message("暂无可导出的分类数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    分类名称: item.categoryName,
    上级分类ID: item.parentId || "0",
    分类层级: item.level,
    排序值: item.sortOrder,
    佣金比例: item.commissionRate,
    支持频道: item.supportChannel ? "是" : "否",
    状态: item.statusLabel,
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "分类管理");
  writeFile(workbook, "分类管理.xlsx");
  message("分类数据导出成功", { type: "success" });
}

async function saveBrandBinding() {
  const categoryId = String(detail.value.id || "");
  if (!categoryId) {
    message("当前分类信息缺失，无法保存关联品牌", { type: "warning" });
    return;
  }

  const selectedSet = new Set(selectedBrandIds.value);
  const changedBrandIds = brandOptions.value
    .map(option => String(option.id))
    .filter(brandId => {
      const currentIds = brandCategorySnapshot.value[brandId] || [];
      const currentlyBound = currentIds.includes(categoryId);
      const nextBound = selectedSet.has(brandId);
      return currentlyBound !== nextBound;
    });

  if (!changedBrandIds.length) {
    message("关联品牌没有变化", { type: "info" });
    return;
  }

  brandSaving.value = true;
  try {
    await Promise.all(
      changedBrandIds.map(brandId => {
        const currentIds = brandCategorySnapshot.value[brandId] || [];
        const nextIds = selectedSet.has(brandId)
          ? Array.from(new Set([...currentIds, categoryId]))
          : currentIds.filter(id => id !== categoryId);
        return saveCategoryBrandCategories(brandId, nextIds);
      })
    );
    message("关联品牌保存成功", { type: "success" });
    await loadCategoryBrandBindings(categoryId);
  } catch (_error) {
    message("关联品牌保存失败，请确认品牌维度保存接口契约", {
      type: "error"
    });
  } finally {
    brandSaving.value = false;
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="分类管理"
    description="承接平台商品分类列表、分类维护和导出动作，作为平台商品分类真实维护页。"
    api-path="/manager/goods/category"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '启用', value: 'ENABLE' },
      { label: '停用', value: 'DISABLE' }
    ]"
    :quick-actions="[
      { label: '分类新增', value: '已接入', type: 'primary' },
      { label: '分类编辑', value: '已接入', type: 'success' },
      { label: '分类删除', value: '已接入', type: 'warning' },
      { label: '分类导出', value: '已接入', type: 'primary' }
    ]"
    status-label="分类状态"
    keyword-label="分类名称"
    keyword-placeholder="请输入分类名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
      <el-button @click="exportCategory">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="层级">
        <el-input
          v-model="extraFilters.levelKeyword"
          placeholder="请输入分类层级"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="分类详情" size="44%">
    <div v-loading="detailLoading" class="category-detail">
      <div class="category-detail__header">
        <div>
          <h3>{{ detail.categoryName || "-" }}</h3>
          <p>
            上级分类 {{ detail.parentId || "0" }} · 更新时间
            {{ formatAdminDateTime(detail.updateTime || detail.createTime) }}
          </p>
        </div>
        <el-tag type="success" effect="light" round>
          已关联 {{ selectedBrandCount }} 个品牌
        </el-tag>
      </div>

      <el-tabs v-model="detailActiveTab">
        <el-tab-pane label="基础信息" name="basic">
          <el-descriptions :column="1" border>
            <el-descriptions-item
              v-for="item in detailItems"
              :key="item.label"
              :label="item.label"
            >
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="关联品牌" name="brand">
          <div class="category-brand-panel">
            <el-alert
              title="后端按品牌维度保存分类关系；这里按分类视角编排，保存时只增减当前分类在各品牌下的绑定状态。"
              type="info"
              :closable="false"
            />
            <div v-loading="brandLoading" class="category-brand-panel__body">
              <el-empty
                v-if="!brandOptions.length"
                description="暂无可关联品牌"
              />
              <el-checkbox-group
                v-else
                v-model="selectedBrandIds"
                class="category-brand-grid"
              >
                <el-checkbox
                  v-for="brand in brandOptions"
                  :key="brand.id"
                  :label="String(brand.id)"
                  border
                  class="category-brand-card"
                >
                  <div class="category-brand-card__content">
                    <span class="category-brand-card__name">
                      {{ brand.name }}
                    </span>
                    <span class="category-brand-card__meta">
                      {{ brand.logo || "未配置品牌图标" }}
                    </span>
                  </div>
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="category-brand-panel__footer">
              <el-button
                type="primary"
                :loading="brandSaving"
                @click="saveBrandBinding"
              >
                保存关联品牌
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>

  <el-dialog
    v-model="dialogVisible"
    :title="editingRow ? '编辑分类' : '新增分类'"
    width="620px"
  >
    <el-form label-width="96px">
      <el-form-item label="分类名称" required>
        <el-input v-model="form.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="上级分类ID" required>
        <el-input v-model="form.parentId" placeholder="顶级分类请填写 0" />
      </el-form-item>
      <el-form-item label="分类层级">
        <el-input-number v-model="form.level" :min="0" :max="3" style="width: 100%" />
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number
          v-model="form.sortOrder"
          :min="0"
          :max="999"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="佣金比例">
        <el-input-number
          v-model="form.commissionRate"
          :min="0"
          :max="100"
          :precision="2"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="分类图片">
        <el-input v-model="form.image" placeholder="请输入分类图片地址，可留空" />
      </el-form-item>
      <el-form-item label="支持频道">
        <el-switch v-model="form.supportChannel" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.category-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.category-detail__header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.category-detail__header h3 {
  margin: 0 0 8px;
  color: #2f3135;
  font-size: 20px;
  font-weight: 700;
}

.category-detail__header p {
  margin: 0;
  color: #8a8f97;
  line-height: 1.6;
}

.category-brand-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-brand-panel__body {
  min-height: 120px;
}

.category-brand-panel__footer {
  display: flex;
  justify-content: flex-end;
}

.category-brand-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

:deep(.category-brand-card) {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 14px 16px;
  align-items: flex-start;
}

.category-brand-card__content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.category-brand-card__name {
  color: #2f3135;
  font-weight: 600;
  line-height: 1.4;
}

.category-brand-card__meta {
  color: #8a8f97;
  font-size: 12px;
  line-height: 1.5;
  word-break: break-all;
}
</style>
