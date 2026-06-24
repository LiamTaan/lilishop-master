<script setup lang="ts">
import StatsTableShell from "../components/stats-table-shell.vue";
import { getOrderOverview } from "@/api/dashboard";
import { extractApiPayload } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "OrderOverview"
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
  const payload = extractApiPayload<Record<string, any>>(response) ?? {};
  return [
    {
      id: "uv-num",
      metricName: "UV人数",
      metricValue: Number(payload.uvNum ?? 0),
      remark: "访问用户总量"
    },
    {
      id: "order-num",
      metricName: "下单数量",
      metricValue: Number(payload.orderNum ?? 0),
      remark: `下单人数 ${Number(payload.orderMemberNum ?? 0)}`
    },
    {
      id: "order-amount",
      metricName: "下单金额",
      metricValue: formatMoney(payload.orderAmount),
      remark: `下单转化率 ${payload.orderConversionRate || "0.0%"}`
    },
    {
      id: "payment-order-num",
      metricName: "付款订单数",
      metricValue: Number(payload.paymentOrderNum ?? 0),
      remark: `付款人数 ${Number(payload.paymentsNum ?? 0)}`
    },
    {
      id: "payment-amount",
      metricName: "付款金额",
      metricValue: formatMoney(payload.paymentAmount),
      remark: `付款转化率 ${payload.paymentsConversionRate || "0.0%"}`
    },
    {
      id: "refund-order-num",
      metricName: "退款订单数",
      metricValue: Number(payload.refundOrderNum ?? 0),
      remark: `退款金额 ${formatMoney(payload.refundOrderPrice)}`
    }
  ];
}
</script>

<template>
  <StatsTableShell
    title="订单概览"
    description="承接平台订单总览、有效订单量与订单趋势指标。"
    api-path="/manager/statistics/order/overview"
    :fetcher="getOrderOverview"
    :normalize-data="normalizeData"
    :columns="columns"
  />
</template>
