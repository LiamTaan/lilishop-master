import { h } from "vue";
import { ElTag } from "element-plus";

function clientTypeLabel(clientType: string) {
  const value = String(clientType).toUpperCase();
  if (["PC"].includes(value)) return "PC端";
  if (["H5", "WAP"].includes(value)) return "H5端";
  if (["APP", "IOS", "ANDROID"].includes(value)) return "App端";
  if (["MINI", "MP", "WECHAT_MP"].includes(value)) return "小程序";
  return clientType || "-";
}

export const columns: TableColumnList = [
  {
    label: "分类名称",
    prop: "title",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.title || "-"),
        h("div", { class: "biz-title__sub" }, row.subtitle || "-")
      ])
  },
  {
    label: "客户端",
    prop: "clientType",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: "warning", effect: "light", round: true },
        () => clientTypeLabel(row.clientType)
      )
  },
  {
    label: "状态",
    prop: "displayStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: String(row.displayStatus).includes("1") ? "success" : "info", effect: "light", round: true },
        () => (String(row.displayStatus).includes("1") ? "启用" : "停用")
      )
  },
  { label: "排序", prop: "sortOrder", minWidth: 100 },
  { label: "操作", slot: "operation", fixed: "right", width: 170 }
];
