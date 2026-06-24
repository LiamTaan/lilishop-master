<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getPintuanPage } from "@/api/marketing-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "PintuanManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detail = ref<Record<string, any>>({});
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  goodsKeyword: ""
});

function normalizePintuanRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.promotionId,
    promotionName: item.promotionName || item.title || item.promotionTitle || "-",
    goodsName: item.goodsName || item.skuName || item.goodsTitle || "-",
    needMemberNum: item.requiredNum || item.needMemberNum || item.limitNum || "-",
    pintuanPrice: item.pintuanPrice || item.price || item.salesPrice || "-",
    promotionStatus: item.promotionStatus || item.status || "-",
    activityTime:
      item.activityTime ||
      [item.startTime, item.endTime].filter(Boolean).join(" 至 ") ||
      "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    extraFilters.goodsKeyword ? String(item.goodsName).includes(extraFilters.goodsKeyword) : true
  )
);

const summaryCards = computed(() => [
  { label: "活动总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "进行中活动",
    value: filteredData.value.filter(item => String(item.promotionStatus).toUpperCase().includes("START")).length,
    accent: "green" as const,
    hint: "当前有效拼团"
  },
  {
    label: "商品覆盖",
    value: [...new Set(filteredData.value.map(item => item.goodsName))].length,
    accent: "blue" as const,
    hint: "参与拼团商品数"
  },
  { label: "拼团治理", value: "已接入", accent: "purple" as const, hint: "活动治理页" }
]);

const detailItems = computed(() => [
  { label: "活动名称", value: detail.value.promotionName || "-" },
  { label: "商品名称", value: detail.value.goodsName || "-" },
  { label: "成团人数", value: detail.value.needMemberNum || "-" },
  { label: "拼团价", value: `¥ ${detail.value.pintuanPrice || "-"}` },
  { label: "活动状态", value: getPromotionStatusLabel(detail.value.promotionStatus) },
  { label: "活动时间", value: detail.value.activityTime || "-" }
]);

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10
  };
  if (query.keyword) params.promotionName = query.keyword;
  if (query.status) params.promotionStatus = query.status;
  const res = await getPintuanPage(params);
  data.value = extractApiRecords(res).map(normalizePintuanRecord);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.goodsKeyword = "";
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
    title="拼团治理"
    description="承接平台拼团活动查询、上下线与拼团成员治理。"
    api-path="/manager/promotion/pintuan"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' },
      { label: '已关闭', value: 'CLOSE' }
    ]"
    :quick-actions="[
      { label: '拼团状态', value: '已接入', type: 'warning' },
      { label: '商品筛选', value: '前端补充', type: 'success' },
      { label: '活动价格', value: '已强化', type: 'primary' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #filters-extra>
      <el-form-item label="商品名称">
        <el-input
          v-model="extraFilters.goodsKeyword"
          placeholder="请输入商品名称"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="拼团活动详情" size="40%">
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
