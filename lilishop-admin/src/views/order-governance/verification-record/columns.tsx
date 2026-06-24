import { h } from "vue";
import { ElTag } from "element-plus";
import { getVerifyStatusLabel, getVerifyStatusType } from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "订单号",
    prop: "orderSn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.orderSn || "-"),
        h("div", { class: "biz-title__sub" }, row.verificationCode || "-")
      ])
  },
  {
    label: "核销状态",
    prop: "verifyStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(ElTag, { type: getVerifyStatusType(row.verifyStatus), effect: "light", round: true }, () =>
        getVerifyStatusLabel(row.verifyStatus)
      )
  },
  { label: "核销门店", prop: "storeName", minWidth: 180 },
  { label: "核销人", prop: "operatorName", minWidth: 140 },
  { label: "核销时间", prop: "verifyTime", minWidth: 180 },
  { label: "操作", slot: "operation", fixed: "right", width: 120 }
];
