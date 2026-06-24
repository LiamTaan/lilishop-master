import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getProcurementStatusLabel,
  getProcurementStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "采购单号",
    prop: "orderSn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.orderSn || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "-")
      ])
  },
  { label: "代理商", prop: "agentName", minWidth: 160 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "采购金额", prop: "totalAmount", minWidth: 120, cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.totalAmount || "-"}`) },
  { label: "采购数量", prop: "totalQuantity", minWidth: 120 },
  {
    label: "采购状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getProcurementStatusType(row.status),
          effect: "light",
          round: true
        },
        () => getProcurementStatusLabel(row.status)
      )
  },
  { label: "操作", slot: "operation", fixed: "right", width: 160 }
];
