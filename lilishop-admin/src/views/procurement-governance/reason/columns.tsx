import { formatAdminDateTime } from "@/utils/admin-governance";

function getReasonCategoryLabel(category: unknown) {
  const value = String(category || "").toUpperCase();
  if (value === "INBOUND") return "入库";
  if (value === "OUTBOUND") return "出库";
  if (value === "DAMAGE") return "报损";
  if (value === "COUNT") return "盘点";
  return String(category || "-");
}

export { getReasonCategoryLabel };

export const columns: TableColumnList = [
  { label: "原因名称", prop: "reason", minWidth: 220 },
  {
    label: "原因类别",
    prop: "category",
    minWidth: 120,
    formatter: ({ category }) => getReasonCategoryLabel(category)
  },
  {
    label: "更新时间",
    prop: "updateTime",
    minWidth: 170,
    formatter: ({ updateTime }) => formatAdminDateTime(updateTime)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 180 }
];
