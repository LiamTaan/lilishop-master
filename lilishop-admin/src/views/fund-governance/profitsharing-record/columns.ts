import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getBillStatusLabel,
  getBillStatusType,
  getProfitSharingSettlementStatusLabel,
  getProfitSharingSettlementStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  { label: "账单号", prop: "orderSn", minWidth: 220 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  {
    label: "账单金额",
    prop: "billPrice",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.billPrice ?? "-"}`)
  },
  {
    label: "退款金额",
    prop: "refundPrice",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", null, `¥ ${row.refundPrice ?? 0}`)
  },
  {
    label: "分账金额",
    prop: "amount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.amount ?? "-"}`)
  },
  {
    label: "账单状态",
    prop: "auditStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(ElTag, { type: getBillStatusType(row.auditStatus), effect: "light", round: true }, () =>
        getBillStatusLabel(row.auditStatus)
      )
  },
  {
    label: "结算状态",
    prop: "settlementStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getProfitSharingSettlementStatusType(row.settlementStatus),
          effect: "light",
          round: true
        },
        () => getProfitSharingSettlementStatusLabel(row.settlementStatus)
      )
  },
  { label: "创建时间", prop: "settleTime", minWidth: 180 }
];
