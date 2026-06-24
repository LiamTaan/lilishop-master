<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getHomeCategoryPage } from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "HomeCategoryManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  clientKeyword: ""
});

function normalizeHomeCategoryRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.navId,
    title: item.title || item.name || item.navName || "-",
    subtitle: item.subtitle || item.subTitle || item.description || "-",
    clientType: item.clientType || item.way || item.platform || "-",
    displayStatus: item.displayStatus || item.status || item.openType || "-",
    sortOrder: item.sortOrder ?? item.sort ?? "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.clientKeyword ? String(item.clientType).includes(extraFilters.clientKeyword) : true
  )
);

const summaryCards = computed(() => [
  { label: "首页分类数", value: filteredData.value.length, accent: "orange" as const, hint: "首页中间分类宫格" },
  {
    label: "客户端覆盖",
    value: [...new Set(filteredData.value.map(item => item.clientType))].length,
    accent: "blue" as const,
    hint: "多端展示治理"
  },
  {
    label: "启用数量",
    value: filteredData.value.filter(item => String(item.displayStatus).includes("1")).length,
    accent: "green" as const,
    hint: "当前可展示分类"
  },
  { label: "配置接口", value: "shortcutNav", accent: "purple" as const, hint: "沿用现有接口" }
]);

const detailItems = computed(() => [
  { label: "首页分类", value: detail.value.title || "-" },
  { label: "副标题", value: detail.value.subtitle || "-" },
  { label: "客户端", value: detail.value.clientType || "-" },
  { label: "展示状态", value: String(detail.value.displayStatus).includes("1") ? "启用" : "停用" },
  { label: "排序值", value: detail.value.sortOrder || "-" }
]);

async function loadData() {
  const params: Record<string, any> = {};
  if (query.keyword) params.name = query.keyword;
  if (query.status) params.status = query.status;
  const res = await getHomeCategoryPage(params);
  data.value = extractApiRecords(res).map(normalizeHomeCategoryRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.clientKeyword = "";
  loadData();
}

function openDetail(row: Record<string, any>) {
  detail.value = row;
  detailVisible.value = true;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="首页分类配置"
    description="对应原型首页中间分类宫格，当前技术接口仍沿用 shortcutNav。"
    api-path="/manager/other/shortcutNav"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :quick-actions="[
      { label: '首页分类', value: '已对齐命名', type: 'warning' },
      { label: '客户端', value: '前端补筛选', type: 'success' },
      { label: '状态治理', value: '已接入', type: 'primary' }
    ]"
    keyword-label="分类名称"
    keyword-placeholder="请输入首页分类名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="客户端">
        <el-input
          v-model="extraFilters.clientKeyword"
          placeholder="请输入客户端标识"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="首页分类详情" size="38%">
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
</template>
