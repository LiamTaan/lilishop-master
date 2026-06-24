import { h } from "vue";
import { ElTag } from "element-plus";
import {
  getAgentLevelLabel,
  getAgentLevelType,
  getBindStatusLabel,
  getBindStatusType,
  getStoreAuditStatusLabel,
  getStoreAuditStatusType
} from "@/utils/admin-governance";

export const columns: TableColumnList = [
  {
    label: "代理商名称",
    prop: "agentName",
    minWidth: 180,
    cellRenderer: ({ row }) =>
        h("div", { class: "biz-title" }, [
        h("div", { class: "biz-title__main" }, row.agentName || "-"),
        h("div", { class: "biz-title__sub" }, row.mobile || "-")
      ])
  },
  {
    label: "代理等级",
    prop: "agentLevel",
    minWidth: 140,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: getAgentLevelType(row.agentLevel),
          effect: "light",
          round: true
        },
        () => getAgentLevelLabel(row.agentLevel)
      )
  },
  { label: "店铺/绑定", prop: "storeName", minWidth: 220, showOverflowTooltip: true },
  { label: "区域", prop: "regionName", minWidth: 180 },
  {
    label: "身份状态",
    prop: "roleStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        {
          type: row.roleStatus === "ENABLE" ? "success" : "info",
          effect: "light",
          round: true
        },
        () => (row.roleStatus === "ENABLE" ? "已生效" : row.roleStatus || "-")
      )
  },
  {
    label: "绑定状态",
    prop: "bindStatus",
    minWidth: 120,
    cellRenderer: ({ row }) =>
      h(
        ElTag,
        { type: getBindStatusType(row.bindStatus), effect: "light", round: true },
        () => getBindStatusLabel(row.bindStatus)
      )
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
  { label: "操作", slot: "operation", fixed: "right", width: 220 }
];
