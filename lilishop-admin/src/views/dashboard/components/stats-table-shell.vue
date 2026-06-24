<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { message } from "@/utils/message";

const props = withDefaults(
  defineProps<{
    title: string;
    description: string;
    apiPath: string;
    columns: TableColumnList;
    fetcher: (params?: Record<string, any>) => Promise<any>;
    normalizeData: (payload: any) => Record<string, any>[];
    defaultSearchType?: string;
  }>(),
  {
    defaultSearchType: "LAST_SEVEN"
  }
);

const loading = ref(false);
const dataList = ref<Record<string, any>[]>([]);
const query = reactive({
  searchType: props.defaultSearchType
});

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

async function onSearch() {
  loading.value = true;
  try {
    const response = await props.fetcher({
      searchType: query.searchType || undefined
    });
    dataList.value = props.normalizeData(response);
  } catch (error: any) {
    dataList.value = [];
    message(getErrorMessage(error, `${props.title}数据加载失败`), {
      type: "error"
    });
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  query.searchType = props.defaultSearchType;
  onSearch();
}

onMounted(() => {
  onSearch();
});
</script>

<template>
  <div class="stats-shell">
    <el-card shadow="never">
      <template #header>
        <div class="stats-shell__header">
          <div>
            <h2>{{ title }}</h2>
            <p>{{ description }}</p>
          </div>
          <el-tag type="primary">{{ apiPath }}</el-tag>
        </div>
      </template>

      <el-form :inline="true" class="stats-shell__query">
        <el-form-item label="统计范围">
          <el-select
            v-model="query.searchType"
            placeholder="请选择统计范围"
            clearable
            style="width: 160px"
          >
            <el-option label="今日" value="TODAY" />
            <el-option label="昨日" value="YESTERDAY" />
            <el-option label="近7天" value="LAST_SEVEN" />
            <el-option label="近30天" value="LAST_THIRTY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">
            查询
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <pure-table row-key="id" :data="dataList" :columns="columns" />
    </el-card>
  </div>
</template>

<style scoped>
.stats-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.stats-shell__header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.stats-shell__header p {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.stats-shell__query {
  margin-bottom: 8px;
}
</style>
