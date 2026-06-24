import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getStoreBizTypeLabel,
  getStoreAuditStatusLabel,
  getStoreAuditStatusType,
  getStoreStatusLabel,
  getStoreStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "店铺名称",
    prop: "storeName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.storeName || "-"),
        h("div", { class: "biz-title__sub" }, row.storeCategoryName || "-")
      ])
  },
  { label: "联系人", prop: "contactName", minWidth: 140 },
  { label: "联系电话", prop: "contactMobile", minWidth: 150 },
  {
    label: "店铺状态",
    prop: "storeStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: getStoreStatusType(row.storeStatus), effect: "light", round: true },
        () => getStoreStatusLabel(row.storeStatus)
      )
  },
  {
    label: "业务类型",
    prop: "bizType",
    minWidth: 110,
    formatter: ({ bizType }) => getStoreBizTypeLabel(bizType)
  },
  {
    label: "审核状态",
    prop: "auditStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getStoreAuditStatusType(row.auditStatus),
          effect: "light",
          round: true
        },
        () => getStoreAuditStatusLabel(row.auditStatus)
      )
  },
  { label: "操作", slot: "operation", fixed: "right", width: 260 }
];
