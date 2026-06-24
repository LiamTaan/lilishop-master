import { h } from "vue";
import { ElTag } from "element-plus";
import {
  formatAdminDateTime,
  getProcurementStatusLabel,
  getProcurementStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  { label: "采购单号", prop: "orderSn", minWidth: 180 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "制单人", prop: "makerName", minWidth: 120 },
  {
    label: "采购金额",
    prop: "totalAmount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", null, `¥ ${row.totalAmount ?? 0}`)
  },
  { label: "采购数量", prop: "totalQuantity", minWidth: 100 },
  {
    label: "状态",
    prop: "status",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getProcurementStatusType(row.status),
          effect: "light",
          round: true
        },
        () => getProcurementStatusLabel(row.status)
      )
  },
  {
    label: "审核时间",
    prop: "auditTime",
    minWidth: 170,
    formatter: ({ auditTime }) => formatAdminDateTime(auditTime)
  },
  {
    label: "创建时间",
    prop: "createTime",
    minWidth: 170,
    formatter: ({ createTime }) => formatAdminDateTime(createTime)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
