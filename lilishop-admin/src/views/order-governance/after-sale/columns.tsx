import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getAfterSaleStatusLabel,
  getAfterSaleStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "售后单号",
    prop: "afterSaleSn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.afterSaleSn || "-"),
        h("div", { class: "biz-title__sub" }, row.orderSn || "-")
      ])
  },
  { label: "商品名称", prop: "goodsName", minWidth: 180 },
  { label: "售后类型", prop: "applyType", minWidth: 120 },
  {
    label: "售后状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(ElTag, { type: getAfterSaleStatusType(row.status), effect: "light", round: true }, () =>
        getAfterSaleStatusLabel(row.status)
      )
  },
  {
    label: "申请金额",
    prop: "applyAmount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.applyAmount || "-"}`)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 180 }
];
