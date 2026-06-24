import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getEnableStatusLabel,
  getEnableStatusType,
  getProfitSharingRoleLabel
} from "@/utils/admin-governance";

function renderScopeCell(primary: string, secondary?: string) {
  return h("div", { class: "biz-title" }, [
    h("div", { class: "biz-title__main" }, primary || "-"),
    h("div", { class: "biz-title__sub" }, secondary || "-")
  ]);
}

export const columns: TableColumnList = [
  {
    label: "规则名称",
    prop: "ruleName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.ruleName || "-"),
        h("div", { class: "biz-title__sub" }, row.regionName || "-")
      ])
  },
  {
    label: "角色类型",
    prop: "roleTypeLabel",
    minWidth: 140,
    cellRenderer: ({ row }) =>
      renderScopeCell(getProfitSharingRoleLabel(row.roleType), row.roleType || "-")
  },
  {
    label: "适用区域",
    prop: "regionName",
    minWidth: 180,
    cellRenderer: ({ row }) =>
      renderScopeCell(row.regionName || "全部区域", row.regionId || "未限制区域")
  },
  {
    label: "适用品类",
    prop: "categoryName",
    minWidth: 180,
    cellRenderer: ({ row }) =>
      renderScopeCell(row.categoryName || "全品类", row.categoryId || "未限制品类")
  },
  {
    label: "分账比例",
    prop: "ratio",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h("span", { class: "money-text" }, `${row.ratio ?? 0}%`)
  },
  {
    label: "状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getEnableStatusType(row.status),
          effect: "light",
          round: true
        },
        () => getEnableStatusLabel(row.status)
      )
  },
  { label: "操作", slot: "operation", fixed: "right", width: 220 }
];
