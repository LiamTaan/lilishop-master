import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getFulfillmentTypeLabel,
  getPrimaryActionLabel,
  getOrderStatusLabel,
  getOrderTypeLabel
} from "@/utils/admin-governance";

function getDisplayStatusType(row: Record<string, any>) {
  if (row.primaryAction === "VERIFY" || row.primaryAction === "RECEIVE") {
    return "warning";
  }
  if (String(row.orderStatus).toUpperCase().includes("COMPLETE")) {
    return "success";
  }
  if (String(row.orderStatus).toUpperCase().includes("CANCEL")) {
    return "danger";
  }
  return "info";
}

export const columns: TableColumnList = [
  {
    label: "订单号",
    prop: "orderSn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.orderSn || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "")
      ])
  },
  {
    label: "商品信息",
    prop: "goodsSummary",
    minWidth: 220,
    showOverflowTooltip: true
  },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "下单用户", prop: "memberName", minWidth: 140 },
  {
    label: "展示状态",
    prop: "displayStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: getDisplayStatusType(row), effect: "light", round: true },
        () => row.displayStatus || getOrderStatusLabel(row.orderStatus)
      )
  },
  {
    label: "订单类型",
    prop: "orderTypeDisplay",
    minWidth: 120,
    formatter: ({ orderTypeDisplay }) => getOrderTypeLabel(orderTypeDisplay)
  },
  {
    label: "履约方式",
    prop: "fulfillmentType",
    minWidth: 120,
    formatter: ({ fulfillmentType }) => getFulfillmentTypeLabel(fulfillmentType)
  },
  {
    label: "主操作",
    prop: "primaryAction",
    minWidth: 120,
    formatter: ({ primaryAction }) => getPrimaryActionLabel(primaryAction)
  },
  {
    label: "实付金额",
    prop: "payableAmount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.payableAmount || "-"}`)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 100 }
];
