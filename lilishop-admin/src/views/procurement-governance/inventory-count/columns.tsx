import { formatAdminDateTime } from "@/utils/admin-governance";

export const columns: TableColumnList = [
  { label: "盘点单号", prop: "sn", minWidth: 180 },
  { label: "店铺ID", prop: "storeId", minWidth: 160 },
  { label: "制单人", prop: "makerName", minWidth: 120 },
  { label: "商品总数", prop: "itemTotal", minWidth: 100 },
  {
    label: "盘点时间",
    prop: "countTime",
    minWidth: 170,
    formatter: ({ countTime }) => formatAdminDateTime(countTime)
  },
  {
    label: "创建时间",
    prop: "createTime",
    minWidth: 170,
    formatter: ({ createTime }) => formatAdminDateTime(createTime)
  },
  { label: "操作", slot: "operation", fixed: "right", width: 180 }
];
