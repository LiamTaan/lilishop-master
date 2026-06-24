import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getPromotionStatusLabel,
  getPromotionStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "积分商品",
    prop: "goodsName",
    minWidth: 240,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.goodsName || "-"),
        h(
          "div",
          { class: "biz-title__sub" },
          `${row.goodsCode || "-"} · ${row.categoryName || "-"}`
        )
      ])
  },
  {
    label: "所需积分",
    prop: "points",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h("span", { class: "money-text" }, `${row.points || "-"} 积分`)
  },
  { label: "库存", prop: "stock", minWidth: 110 },
  { label: "兑换上限", prop: "exchangeLimit", minWidth: 120 },
  { label: "适用店铺", prop: "storeName", minWidth: 180 },
  {
    label: "状态",
    prop: "promotionStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getPromotionStatusType(row.promotionStatus),
          effect: "light",
          round: true
        },
        () => getPromotionStatusLabel(row.promotionStatus)
      )
  }
];
