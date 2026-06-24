import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getWalletServiceTypeLabel,
  getWalletServiceTypeTagType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "流水号",
    prop: "id",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.id || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "-")
      ])
  },
  { label: "代理商", prop: "agentName", minWidth: 160 },
  {
    label: "业务类型",
    prop: "serviceType",
    minWidth: 140,
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
  { label: "变动金额", prop: "money", minWidth: 120, cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.money || "-"}`) },
  { label: "业务描述", prop: "detail", minWidth: 260 },
  {
    label: "操作",
    slot: "operation",
    fixed: "right",
    width: 160
  }
];
