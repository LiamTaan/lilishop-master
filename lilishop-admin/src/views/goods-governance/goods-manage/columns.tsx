import { h } from "vue";
import { ElTag } from "element-plus";
import ReText from "@/components/ReText";
import {
  getGoodsAuditStatusLabel,
  getGoodsAuditStatusType,
  getGoodsMarketStatusLabel,
  getGoodsMarketStatusType,
  getGoodsTypeLabel
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "商品图片",
    prop: "thumb",
    width: 104,
    cellRenderer: ({ row }) =>
      h(
        "div",
        { class: "goods-thumb" },
        row.thumb
          ? [
              h("img", {
                class: "goods-thumb__img",
                src: row.thumb,
                alt: row.goodsName || "goods"
              })
            ]
          : [h("div", { class: "goods-thumb__placeholder" }, "暂无")]
      )
  },
  {
    label: "商品标题",
    prop: "goodsName",
    minWidth: 280,
    cellRenderer: ({ row }) => {
      const title = String(row.goodsName || "-");
      return (
      h("div", { class: "goods-title" }, [
        h(
          ReText,
          { lineClamp: 2, class: "goods-title__name" },
          {
            default: () => title,
            content: () => title
          }
        ),
        h("div", { class: "goods-title__sub" }, row.storeName || "-")
      ])
      );
    }
  },
  { label: "商品编码", prop: "goodsCode", minWidth: 160 },
  {
    label: "商品类型",
    prop: "goodsType",
    minWidth: 120,
    formatter: ({ goodsType }) => getGoodsTypeLabel(goodsType)
  },
  { label: "规格", prop: "specText", minWidth: 110 },
  {
    label: "成本价",
    prop: "costPrice",
    minWidth: 110,
    cellRenderer: ({ row }) =>
      h("span", { class: "money-text" }, `¥ ${row.costPrice || "-"}`)
  },
  {
    label: "售价/起批价",
    prop: "salePrice",
    minWidth: 110,
    cellRenderer: ({ row }) =>
      h("span", { class: "money-text" }, `¥ ${row.salePrice || "-"}`)
  },
  {
    label: "利润/毛利",
    prop: "profitAmount",
    minWidth: 110,
    cellRenderer: ({ row }) =>
      h(
        "span",
        { class: "money-text money-text--profit" },
        row.profitAmount == null ? "¥ -" : `¥ ${row.profitAmount}`
      )
  },
  { label: "库存", prop: "stock", minWidth: 90 },
  {
    label: "销售状态",
    prop: "marketEnable",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getGoodsMarketStatusType(row.marketEnable),
          effect: "light",
          round: true
        },
        () => getGoodsMarketStatusLabel(row.marketEnable)
      )
  },
  {
    label: "审核状态",
    prop: "authFlag",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getGoodsAuditStatusType(row.authFlag),
          effect: "light",
          round: true
        },
        () => getGoodsAuditStatusLabel(row.authFlag)
      )
  },
  { label: "创建日期", prop: "createTime", minWidth: 160 },
  { label: "操作", slot: "operation", fixed: "right", width: 240 }
];
