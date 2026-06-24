import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getWalletServiceTypeLabel,
  getWalletServiceTypeTagType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "流水号",
    prop: "sn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.sn || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "-")
      ])
  },
  { label: "会员名称", prop: "memberName", minWidth: 160 },
  {
    label: "流水类型",
    prop: "serviceType",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getWalletServiceTypeTagType(row.serviceType),
          effect: "light",
          round: true
        },
        () => getWalletServiceTypeLabel(row.serviceType)
      )
  },
  { label: "变动金额", prop: "amount", minWidth: 120, cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.amount || "-"}`) },
  { label: "当前余额", prop: "balanceAfter", minWidth: 120, cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.balanceAfter || "-"}`) }
];
