import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getAgentLevelLabel,
  getAgentLevelType,
  getStoreAuditStatusLabel,
  getStoreAuditStatusType,
  getStoreBizTypeLabel
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "店铺名称",
    prop: "storeName",
    minWidth: 220,
    cellRenderer: ({ row }) =>
      h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.storeName || "-"),
        h("div", { class: "biz-title__sub" }, row.contactName || "-")
      ])
  },
  {
    label: "业务类型",
    prop: "bizType",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: row.bizType === "AGENT" ? "warning" : "success",
          effect: "light",
          round: true
        },
        () => getStoreBizTypeLabel(row.bizType)
      )
  },
  { label: "主体类型", prop: "applyType", minWidth: 120 },
  {
    label: "代理等级",
    prop: "agentLevel",
    minWidth: 140,
    cellRenderer: ({ row }) =>
      row.bizType === "AGENT"
        ? h(
            ElTag,
            {
              type: getAgentLevelType(row.agentLevel),
              effect: "light",
              round: true
            },
            () => getAgentLevelLabel(row.agentLevel)
          )
        : h("span", "-")
  },
  { label: "区域", prop: "regionName", minWidth: 180, showOverflowTooltip: true },
  { label: "联系人", prop: "contactName", minWidth: 140 },
  { label: "联系电话", prop: "contactMobile", minWidth: 150 },
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
  { label: "提交时间", prop: "submitTime", minWidth: 180 },
  { label: "操作", slot: "operation", fixed: "right", width: 180 }
];
