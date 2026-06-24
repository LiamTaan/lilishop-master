import { h } from "vue";

export const columns: TableColumnList = [
  { label: "角色类型", prop: "roleType", minWidth: 120 },
  { label: "账户数量", prop: "accountCount", minWidth: 120 },
  {
    label: "待结算金额",
    prop: "pendingAmount",
    minWidth: 140,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.pendingAmount || "-"}`)
  },
  {
    label: "已结算金额",
    prop: "settledAmount",
    minWidth: 140,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.settledAmount || "-"}`)
  },
  {
    label: "可提现金额",
    prop: "withdrawableAmount",
    minWidth: 140,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.withdrawableAmount || "-"}`)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
