import { h } from "vue";
import { ElTag } from "element-plus";

export const columns: TableColumnList = [
  {
    label: "分类名称",
    prop: "categoryName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.categoryName || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "")
      ])
  },
  {
    label: "层级",
    prop: "level",
    minWidth: 110,
    cellRenderer: ({ row }) =>
      h(ElTag, { type: "warning", effect: "light", round: true }, () => row.level || "-")
  },
  { label: "排序", prop: "sortOrder", minWidth: 100 },
  {
    label: "状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: row.status === "ENABLE" ? "success" : "info",
          effect: "light",
          round: true
        },
        () => row.statusLabel || "停用"
      )
  },
  { label: "创建时间", prop: "createTime", minWidth: 180 },
  { label: "操作", slot: "operation", fixed: "right", width: 220 }
];
