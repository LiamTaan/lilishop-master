import { h } from "vue";
import { ElTag } from "element-plus";
import { formatAdminDateTime } from "@/utils/admin-governance";

function getDamageStatusLabel(status: unknown) {
  const value = String(status || "").toUpperCase();
  if (value === "DRAFT") return "草稿";
  if (value === "SUBMITTED") return "已提交";
  if (value === "APPROVED") return "已通过";
  if (value === "REJECTED") return "已驳回";
  if (value === "CANCELLED") return "已取消";
  if (value === "COMPLETED") return "已完成";
  return String(status || "-");
}

function getDamageStatusType(status: unknown) {
  const value = String(status || "").toUpperCase();
  if (["DRAFT", "SUBMITTED"].includes(value)) return "warning";
  if (["APPROVED", "COMPLETED"].includes(value)) return "success";
  if (["REJECTED", "CANCELLED"].includes(value)) return "danger";
  return "info";
}

export { getDamageStatusLabel };

export const columns: TableColumnList = [
  { label: "报损单号", prop: "sn", minWidth: 180 },
  { label: "店铺ID", prop: "storeId", minWidth: 160 },
  {
    label: "报损金额",
    prop: "totalAmount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", null, `¥ ${row.totalAmount ?? 0}`)
  },
  { label: "报损数量", prop: "totalQuantity", minWidth: 100 },
  {
    label: "状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: getDamageStatusType(row.status), effect: "light", round: true },
        () => getDamageStatusLabel(row.status)
      )
  },
  {
    label: "报损日期",
    prop: "damageDate",
    minWidth: 150,
    formatter: ({ damageDate }) => formatAdminDateTime(damageDate)
  },
  {
    label: "创建时间",
    prop: "createTime",
    minWidth: 170,
    formatter: ({ createTime }) => formatAdminDateTime(createTime)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
