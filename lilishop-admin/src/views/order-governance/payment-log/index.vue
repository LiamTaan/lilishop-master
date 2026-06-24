<script setup lang="ts">
import GenericListPage from "@/views/super-admin/shared/generic-list-page.vue";
import { getPaymentLogPage } from "@/api/order-governance";

defineOptions({
  name: "PaymentLogManage"
});

const pageConfig = {
  pageTitle: "支付日志",
  keywordField: "orderSn",
  listApi: getPaymentLogPage,
  statusOptions: [
    { label: "支付成功", value: "SUCCESS" },
    { label: "支付处理中", value: "PENDING" },
    { label: "支付失败", value: "FAIL" }
  ],
  normalizeRecord: (item: Record<string, any>) => ({
    displayName: item.orderSn || item.sn || item.id || "-",
    displayStatus: item.paymentStatus || item.status || item.payStatus || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.paymentMethod || item.paymentName || item.clientType || "-"
  }),
  detailFields: [
    { key: "orderSn", label: "订单号" },
    { key: "paymentMethod", label: "支付方式" },
    { key: "paymentStatus", label: "支付状态" },
    { key: "flowPrice", label: "支付金额" },
    { key: "receivableNo", label: "平台支付单号" },
    { key: "createTime", label: "创建时间" }
  ]
};
</script>

<template>
  <GenericListPage
    title="支付日志"
    description="承接平台支付流水记录查看，覆盖支付方式、金额和状态回执。"
    api-path="/manager/order/paymentLog"
    keyword-label="订单号"
    keyword-placeholder="请输入订单号"
    :page-config="pageConfig"
  />
</template>
