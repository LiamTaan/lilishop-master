<script setup lang="ts">
import GenericListPage from "@/views/super-admin/shared/generic-list-page.vue";
import { getOrderLogPage } from "@/api/order-governance";

defineOptions({
  name: "OrderLogManage"
});

const pageConfig = {
  pageTitle: "订单日志",
  keywordField: "orderSn",
  listApi: getOrderLogPage,
  statusOptions: [
    { label: "创建", value: "CREATE" },
    { label: "支付", value: "PAY" },
    { label: "发货", value: "DELIVER" },
    { label: "退款", value: "REFUND" }
  ],
  normalizeRecord: (item: Record<string, any>) => ({
    displayName: item.orderSn || item.id || "-",
    displayStatus: item.orderStatus || item.logType || item.status || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.message || item.detail || item.operator || "-"
  }),
  detailFields: [
    { key: "orderSn", label: "订单号" },
    { key: "logType", label: "日志类型" },
    { key: "orderStatus", label: "订单状态" },
    { key: "operator", label: "操作人" },
    { key: "message", label: "日志内容" },
    { key: "createTime", label: "创建时间" }
  ]
};
</script>

<template>
  <GenericListPage
    title="订单日志"
    description="承接订单流转操作日志查看，覆盖关键节点和操作人信息。"
    api-path="/manager/order/orderLog/getByPage"
    keyword-label="订单号"
    keyword-placeholder="请输入订单号"
    :page-config="pageConfig"
  />
</template>
