import { h } from "vue";
import { ElTag } from "element-plus";

export const columns: TableColumnList = [
  {
    label: "店铺名称",
    prop: "storeName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.storeName || "-"),
        h("div", { class: "biz-title__sub" }, row.operateTime || "-")
      ])
  },
  { label: "审核动作", prop: "auditAction", minWidth: 140 },
  {
    label: "审核结果",
    prop: "auditResult",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(ElTag, { type: "warning", effect: "light", round: true }, () => row.auditResult || "-")
  },
  { label: "审核备注", prop: "auditRemark", minWidth: 240 },
  { label: "操作人", prop: "operatorName", minWidth: 140 },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
