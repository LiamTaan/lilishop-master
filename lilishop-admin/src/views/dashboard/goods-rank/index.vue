<script setup lang="ts">
import StatsTableShell from "../components/stats-table-shell.vue";
import { getGoodsRank } from "@/api/dashboard";
import { extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "GoodsRank"
});

function formatMoney(value: unknown) {
  const amount = Number(value ?? 0);
  if (!Number.isFinite(amount)) return "0.00";
  return amount.toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function normalizeData(response: any) {
  return extractApiRecords(response).map((item, index) => ({
    id: item.goodsId || `goods-${index + 1}`,
    rankNo: index + 1,
    goodsOrCategoryName: item.goodsName || item.categoryName || "-",
    salesAmount: formatMoney(item.price),
    salesNum: Number(item.num ?? 0)
  }));
}
</script>

<template>
  <StatsTableShell
    title="商品/品类排行"
    description="承接平台热卖商品、类目销售额与销量排行统计。"
    api-path="/manager/statistics/index/goodsStatistics"
    :fetcher="getGoodsRank"
    :normalize-data="normalizeData"
    :columns="columns"
  />
</template>
