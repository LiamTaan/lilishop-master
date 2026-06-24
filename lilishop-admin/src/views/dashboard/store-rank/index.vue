<script setup lang="ts">
import StatsTableShell from "../components/stats-table-shell.vue";
import { getStoreRank } from "@/api/dashboard";
import { extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "StoreRank"
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
    id: item.storeId || `store-${index + 1}`,
    rankNo: index + 1,
    storeName: item.storeName || "-",
    salesAmount: formatMoney(item.price),
    orderCount: Number(item.num ?? 0)
  }));
}
</script>

<template>
  <StatsTableShell
    title="门店排行"
    description="承接平台热卖店铺排行、门店销售额与订单数统计。"
    api-path="/manager/statistics/index/storeStatistics"
    :fetcher="getStoreRank"
    :normalize-data="normalizeData"
    :columns="columns"
  />
</template>
