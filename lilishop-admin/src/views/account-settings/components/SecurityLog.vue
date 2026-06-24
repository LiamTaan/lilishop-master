<script setup lang="ts">
import dayjs from "dayjs";
import { getMineLogs } from "@/api/user";
import { useUserStoreHook } from "@/store/modules/user";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { isSuccessResult } from "@/utils/result";
import { reactive, ref, onMounted } from "vue";
import { deviceDetection } from "@pureadmin/utils";
import type { PaginationProps } from "@pureadmin/table";

defineOptions({
  name: "SecurityLog"
});

const loading = ref(true);
const userStore = useUserStoreHook();
const dataList = ref([]);
const pagination = reactive<PaginationProps>({
  total: 0,
  pageSize: 10,
  currentPage: 1,
  background: true,
  align: "right",
  layout: "prev, pager, next"
});
const columns: TableColumnList = [
  {
    label: "操作名称",
    prop: "name",
    minWidth: 160
  },
  {
    label: "请求地址",
    prop: "requestUrl",
    minWidth: 220
  },
  {
    label: "IP 地址",
    prop: "ip",
    minWidth: 120
  },
  {
    label: "附加信息",
    prop: "ipInfo",
    minWidth: 180
  },
  {
    label: "时间",
    prop: "createTime",
    minWidth: 180,
    formatter: ({ createTime }) =>
      createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "-"
  }
];

function buildListParams() {
  return {
    pageNumber: pagination.currentPage,
    pageSize: pagination.pageSize,
    operatorName: userStore.username,
    searchKey: ""
  };
}

async function onSearch() {
  loading.value = true;
  const response = await getMineLogs(buildListParams());
  if (isSuccessResult(response)) {
    const payload = extractApiPayload<Record<string, any>>(response) || {};
    const records = extractApiRecords<Record<string, any>>(response);
    dataList.value = records.map(item => ({
      id: String(item.id ?? ""),
      name: item.name || item.customerLog || "-",
      requestUrl: item.requestUrl || "-",
      ip: item.ip || "-",
      ipInfo: item.ipInfo || "-",
      createTime: item.createTime
    }));
    pagination.total = Number(payload.total ?? records.length ?? 0);
    pagination.pageSize = Number(payload.size ?? pagination.pageSize);
    pagination.currentPage = Number(payload.current ?? pagination.currentPage);
  }

  setTimeout(() => {
    loading.value = false;
  }, 200);
}

async function handleCurrentChange(val: number) {
  pagination.currentPage = val;
  await onSearch();
}

onMounted(() => {
  onSearch();
});
</script>

<template>
  <div :class="['min-w-45', deviceDetection() ? 'max-w-full' : 'max-w-[70%]']">
    <h3 class="my-8!">安全日志</h3>
    <pure-table
      row-key="id"
      table-layout="auto"
      :loading="loading"
      :data="dataList"
      :columns="columns"
      :pagination="pagination"
      @page-current-change="handleCurrentChange"
    />
  </div>
</template>
