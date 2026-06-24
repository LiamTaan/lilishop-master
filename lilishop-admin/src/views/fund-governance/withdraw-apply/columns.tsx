import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getWithdrawApplyStatusLabel,
  getWithdrawApplyStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "申请单号",
    prop: "applySn",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.applySn || "-"),
        h("div", { class: "biz-title__sub" }, row.createTime || "")
      ])
  },
  { label: "会员名称", prop: "memberName", minWidth: 160 },
  {
    label: "提现金额",
    prop: "withdrawAmount",
    minWidth: 120,
    cellRenderer: ({ row }) => h("span", { class: "money-text" }, `¥ ${row.withdrawAmount || "-"}`)
  },
  { label: "收款方式", prop: "accountType", minWidth: 120 },
  {
    label: "申请状态",
    prop: "applyStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getWithdrawApplyStatusType(row.applyStatus),
          effect: "light",
          round: true
        },
        () => getWithdrawApplyStatusLabel(row.applyStatus)
      )
  },
  { label: "申请时间", prop: "createTime", minWidth: 180 }
];
