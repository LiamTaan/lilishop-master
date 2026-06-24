import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getPromotionStatusLabel,
  getPromotionStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "卡券名称",
    prop: "cardCouponName",
    minWidth: 240,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.cardCouponName || "-"),
        h(
          "div",
          { class: "biz-title__sub" },
          `${row.cardCouponCode || "-"} · ${row.cardCouponType || "-"}`
        )
      ])
  },
  { label: "适用商品", prop: "goodsName", minWidth: 180 },
  {
    label: "面值",
    prop: "faceValue",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h("span", { class: "money-text" }, String(row.faceValue || "-"))
  },
  { label: "库存", prop: "stock", minWidth: 110 },
  { label: "领取上限", prop: "receiveLimit", minWidth: 120 },
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
