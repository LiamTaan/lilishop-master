import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getPromotionStatusLabel,
  getPromotionStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "活动名称",
    prop: "promotionName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.promotionName || "-"),
        h("div", { class: "biz-title__sub" }, row.goodsName || "-")
      ])
  },
  { label: "成团人数", prop: "needMemberNum", minWidth: 120 },
  {
    label: "拼团价",
    prop: "pintuanPrice",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.pintuanPrice || "-"}`)
  },
  {
    label: "活动状态",
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
  },
  { label: "活动时间", prop: "activityTime", minWidth: 220 },
  { label: "操作", slot: "operation", fixed: "right", width: 180 }
];
