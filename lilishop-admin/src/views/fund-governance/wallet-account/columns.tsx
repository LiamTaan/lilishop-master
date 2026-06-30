import { h } from "vue";

export const columns: TableColumnList = [
  {
    label: "会员名称",
    prop: "memberName",
    minWidth: 180,
    cellRenderer: ({ row }) => {
      const memberName = row.memberName || "-";
      const mobile = row.mobile || "";
      const children = [h("div", { class: "biz-title__main" }, memberName)];
      if (mobile && mobile !== memberName) {
        children.push(h("div", { class: "biz-title__sub" }, mobile));
      }
      return h("div", { class: "biz-title" }, children);
    }
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
