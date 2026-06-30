import { h } from "vue";
import { ElTag } from "element-plus";
import { getStoreAuditStatusType } from "@/utils/admin-governance";

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
  { label: "审核动作", prop: "auditActionLabel", minWidth: 180 },
  {
    label: "审核结果",
    prop: "auditResultLabel",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getStoreAuditStatusType(row.auditResult),
          effect: "light",
          round: true
        },
        () => row.auditResultLabel || "-"
      )
  },
  { label: "审核备注", prop: "auditRemark", minWidth: 240 },
  { label: "操作人", prop: "operatorName", minWidth: 140 },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
