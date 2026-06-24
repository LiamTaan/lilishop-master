<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import {
  getPintuanDetail,
  getPintuanGoodsPage,
  getPintuanMembers,
  getPintuanPage
} from "@/api/marketing-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "PintuanRecord"
});

const route = useRoute();
const pageTitle = computed(() => String(route.meta.title || "拼团记录"));
const detailVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const rows = ref<Record<string, any>[]>([]);
const memberRows = ref<Record<string, any>[]>([]);
const goodsRows = ref<Record<string, any>[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  goodsKeyword: ""
});

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "关联商品", prop: "goodsName", minWidth: 220, showOverflowTooltip: true },
  { label: "成团人数", prop: "needMemberNum", minWidth: 120 },
  { label: "活动状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "活动时间", prop: "activityTime", minWidth: 220 },
  { label: "操作", prop: "operation", width: 140, fixed: "right", slot: "operation" }
];

const filteredData = computed(() =>
  rows.value.filter(item =>
    extraFilters.goodsKeyword
      ? String(item.goodsName || "").includes(extraFilters.goodsKeyword)
      : true
  )
);

const summaryCards = computed(() => [
  {
    label: "活动记录数",
    value: filteredData.value.length,
    accent: "orange" as const,
    hint: "当前拼团活动记录"
  },
  {
    label: "进行中活动",
    value: filteredData.value.filter(item => item.promotionStatusCode === "START")
      .length,
    accent: "green" as const,
    hint: "当前有效活动"
  },
  {
    label: "待成团活动",
    value: filteredData.value.filter(item => item.promotionStatusCode !== "END")
      .length,
    accent: "blue" as const,
    hint: "可继续查看成员进度"
  },
  {
    label: "记录明细",
    value: "成员/商品",
    accent: "purple" as const,
    hint: "承接真实拼团明细接口"
  }
]);

function normalizeRow(item: Record<string, any>) {
  const statusCode = String(
    item.promotionStatus || item.status || item.pintuanStatus || ""
  )
    .trim()
    .toUpperCase();
  return {
    ...item,
    id: item.id || item.promotionId,
    promotionName: item.promotionName || item.title || item.promotionTitle || "-",
    goodsName: item.goodsName || item.skuName || item.goodsTitle || "-",
    needMemberNum: item.requiredNum || item.needMemberNum || item.limitNum || "-",
    activityTime:
      item.activityTime ||
      [item.startTime, item.endTime].filter(Boolean).join(" 至 ") ||
      "-",
    promotionStatusCode: statusCode,
    promotionStatusLabel: getPromotionStatusLabel(statusCode)
  };
}

function normalizeMember(item: Record<string, any>, index: number) {
  const status = String(
    item.memberStatus || item.status || item.joinStatus || item.orderStatus || ""
  )
    .trim()
    .toUpperCase();
  return {
    id: item.id || item.memberId || item.sn || `member-${index}`,
    memberName: item.memberName || item.nickname || item.username || "-",
    mobile: item.mobile || item.memberMobile || "-",
    joinStatus:
      status === "LEADER"
        ? "团长"
        : status === "SUCCESS"
          ? "已成团"
          : status === "WAIT"
            ? "待成团"
            : status || "-",
    joinTime: item.createTime || item.joinTime || item.updateTime || "-"
  };
}

function normalizeGoods(item: Record<string, any>, index: number) {
  return {
    id: item.id || item.skuId || `goods-${index}`,
    goodsName: item.goodsName || item.skuName || "-",
    originalPrice: item.originalPrice || item.price || "-",
    pintuanPrice: item.promotionPrice || item.pintuanPrice || item.settlementPrice || "-",
    stock: item.quantity || item.stock || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 10
    };
    if (query.keyword) params.promotionName = query.keyword;
    if (query.status) params.promotionStatus = query.status;
    const res = await getPintuanPage(params);
    rows.value = extractApiRecords(res).map(normalizeRow);
  } catch (_error) {
    message("拼团记录加载失败，请稍后重试", { type: "error" });
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
  extraFilters.goodsKeyword = "";
  loadData();
}

async function openDetail(row: Record<string, any>) {
  try {
    const [detailRes, memberRes, goodsRes] = await Promise.all([
      getPintuanDetail(String(row.id)),
      getPintuanMembers(String(row.id)),
      getPintuanGoodsPage(String(row.id), { pageNumber: 1, pageSize: 20 })
    ]);
    currentRow.value = normalizeRow(
      (extractApiPayload(detailRes) || row) as Record<string, any>
    );
    memberRows.value = extractApiRecords(memberRes).map(normalizeMember);
    goodsRows.value = extractApiRecords(goodsRes).map(normalizeGoods);
  } catch (_error) {
    currentRow.value = row;
    memberRows.value = [];
    goodsRows.value = [];
    message("拼团记录明细加载失败，已展示基础活动信息", {
      type: "warning"
    });
  }
  detailVisible.value = true;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    :title="pageTitle"
    description="承接拼团活动记录、待成团成员明细和关联商品查询，避免菜单落到只有规则没有过程记录。"
    api-path="/manager/promotion/pintuan + /manager/promotion/pintuan/{id}/members"
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
      { label: '活动列表', value: '已接入', type: 'primary' },
      { label: '团员明细', value: '已接入', type: 'success' },
      { label: '关联商品', value: '已接入', type: 'warning' }
    ]"
    keyword-label="活动名称"
    keyword-placeholder="请输入拼团活动名称"
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
      <el-button link type="primary" @click="openDetail(row)">查看记录</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="拼团记录详情" size="760px">
    <el-descriptions v-if="currentRow" :column="2" border class="mb-4">
      <el-descriptions-item label="活动名称">
        {{ currentRow.promotionName }}
      </el-descriptions-item>
      <el-descriptions-item label="活动状态">
        {{ currentRow.promotionStatusLabel }}
      </el-descriptions-item>
      <el-descriptions-item label="关联商品">
        {{ currentRow.goodsName }}
      </el-descriptions-item>
      <el-descriptions-item label="成团人数">
        {{ currentRow.needMemberNum }}
      </el-descriptions-item>
      <el-descriptions-item label="活动时间" :span="2">
        {{ currentRow.activityTime }}
      </el-descriptions-item>
    </el-descriptions>

    <h3 class="drawer-title">团员记录</h3>
    <el-table :data="memberRows" border class="mb-4">
      <el-table-column label="成员名称" prop="memberName" min-width="180" />
      <el-table-column label="手机号" prop="mobile" min-width="160" />
      <el-table-column label="参与状态" prop="joinStatus" min-width="120" />
      <el-table-column label="参与时间" prop="joinTime" min-width="180" />
    </el-table>

    <h3 class="drawer-title">关联商品</h3>
    <el-table :data="goodsRows" border>
      <el-table-column label="商品名称" prop="goodsName" min-width="220" />
      <el-table-column label="原价" prop="originalPrice" min-width="120" />
      <el-table-column label="拼团价" prop="pintuanPrice" min-width="120" />
      <el-table-column label="库存" prop="stock" min-width="120" />
    </el-table>
  </el-drawer>
</template>

<style scoped>
.drawer-title {
  margin: 0 0 12px;
  color: #2d2f33;
  font-size: 16px;
}
</style>
