<script setup lang="ts">
import GenericListPage from "@/views/super-admin/shared/generic-list-page.vue";
import { getReceiptPage } from "@/api/order-governance";

defineOptions({
  name: "ReceiptManage"
});

const pageConfig = {
  pageTitle: "发票管理",
  keywordField: "orderSn",
  listApi: getReceiptPage,
  statusOptions: [
    { label: "已开票", value: "OPENED" },
    { label: "待开票", value: "NEW" },
    { label: "已作废", value: "CANCELLED" }
  ],
  normalizeRecord: (item: Record<string, any>) => ({
    displayName: item.orderSn || item.receiptTitle || item.id || "-",
    displayStatus: item.receiptStatus || item.status || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayRemark: item.taxpayerId || item.memberName || item.storeName || "-"
  }),
  detailFields: [
    { key: "orderSn", label: "订单号" },
    { key: "receiptTitle", label: "发票抬头" },
    { key: "receiptType", label: "发票类型" },
    { key: "receiptStatus", label: "发票状态" },
    { key: "taxpayerId", label: "税号" },
    { key: "memberName", label: "申请人" },
    { key: "createTime", label: "申请时间" }
  ]
};
</script>

<template>
  <GenericListPage
    title="发票管理"
    description="承接电子发票申请记录与抬头信息查看，便于后台审计与客服排障。"
    api-path="/manager/trade/receipt"
    keyword-label="订单号"
    keyword-placeholder="请输入订单号"
    :page-config="pageConfig"
  />
</template>
