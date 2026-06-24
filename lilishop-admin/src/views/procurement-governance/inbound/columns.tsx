import { h } from "vue";
import { formatAdminDateTime } from "@/utils/admin-governance";

export const columns: TableColumnList = [
  { label: "入库单号", prop: "inboundSn", minWidth: 180 },
  { label: "采购单ID", prop: "procurementOrderId", minWidth: 180 },
  { label: "店铺ID", prop: "storeId", minWidth: 160 },
  { label: "制单人", prop: "operatorName", minWidth: 120 },
  { label: "预计入库量", prop: "expectedQuantity", minWidth: 110 },
  { label: "已确认入库量", prop: "confirmedQuantity", minWidth: 120 },
  { label: "待确认入库量", prop: "pendingQuantity", minWidth: 120 },
  {
    label: "入库成本",
    prop: "totalCost",
    minWidth: 110,
    cellRenderer: ({ row }) => h("span", null, `¥ ${row.totalCost ?? 0}`)
  },
  {
    label: "零售金额",
    prop: "totalRetailAmount",
    minWidth: 110,
    cellRenderer: ({ row }) =>
      h("span", null, `¥ ${row.totalRetailAmount ?? 0}`)
  },
  {
    label: "入库时间",
    prop: "inboundTime",
    minWidth: 170,
    formatter: ({ inboundTime }) => formatAdminDateTime(inboundTime)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
