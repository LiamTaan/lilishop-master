<script setup lang="ts">
import GenericListPage from "@/views/super-admin/shared/generic-list-page.vue";
import { getRefundLogPage } from "@/api/order-governance";

defineOptions({
  name: "RefundLogManage"
});

const pageConfig = {
  pageTitle: "退款日志",
  keywordField: "orderSn",
  listApi: getRefundLogPage,
  statusOptions: [
    { label: "退款成功", value: "SUCCESS" },
    { label: "退款处理中", value: "PENDING" },
    { label: "退款失败", value: "FAIL" }
  ],
  normalizeRecord: (item: Record<string, any>) => ({
    displayName: item.orderSn || item.sn || item.id || "-",
    displayStatus: item.status || item.payStatus || item.refundStatus || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.errorMessage || item.receivableNo || item.outOrderNo || "-"
  }),
  detailFields: [
    { key: "orderSn", label: "订单号" },
    { key: "sn", label: "退款流水号" },
    { key: "status", label: "退款状态" },
    { key: "amount", label: "退款金额" },
    { key: "receivableNo", label: "支付单号" },
    { key: "outOrderNo", label: "外部单号" },
    { key: "createTime", label: "创建时间" }
  ]
};
</script>

<template>
  <GenericListPage
    title="退款日志"
    description="承接平台退款流水记录查看，便于定位退款单状态与三方回执。"
    api-path="/manager/order/refundLog"
    keyword-label="订单号"
    keyword-placeholder="请输入订单号"
    :page-config="pageConfig"
  />
</template>
