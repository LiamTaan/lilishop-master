import { h } from "vue";

export const columns: TableColumnList = [
  {
    label: "会员名称",
    prop: "memberName",
    minWidth: 180,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.memberName || "-"),
        h("div", { class: "biz-title__sub" }, row.mobile || "-")
      ])
  },
  { label: "余额", prop: "balance", minWidth: 120, cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.balance || "-"}`) },
  {
    label: "可提现金额",
    prop: "withdrawableAmount",
    minWidth: 140,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.withdrawableAmount || "-"}`)
  },
  {
    label: "冻结金额",
    prop: "frozenAmount",
    minWidth: 140,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.frozenAmount || "-"}`)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
