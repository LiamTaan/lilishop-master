import { procurementGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/procurement-governance",
  component: Layout,
  redirect: "/procurement-governance/order",
  meta: {
    icon: "ep:box",
    title: "采购管理",
    rank: procurementGovernance
  },
  children: [
    {
      path: "/procurement-governance/order",
      name: "ProcurementGovernanceOrder",
      component: () => import("@/views/procurement-governance/order/index.vue"),
      meta: { title: "采购订单管理" }
    },
    {
      path: "/procurement-governance/inbound",
      name: "ProcurementGovernanceInbound",
      component: () =>
        import("@/views/procurement-governance/inbound/index.vue"),
      meta: { title: "采购入库管理" }
    },
    {
      path: "/procurement-governance/inventory-count",
      name: "ProcurementGovernanceInventoryCount",
      component: () =>
        import("@/views/procurement-governance/inventory-count/index.vue"),
      meta: { title: "盘点管理" }
    },
    {
      path: "/procurement-governance/damage-report",
      name: "ProcurementGovernanceDamageReport",
      component: () =>
        import("@/views/procurement-governance/damage-report/index.vue"),
      meta: { title: "报损管理" }
    },
    {
      path: "/procurement-governance/reason",
      name: "ProcurementGovernanceReason",
      component: () =>
        import("@/views/procurement-governance/reason/index.vue"),
      meta: { title: "库存原因管理" }
    }
  ]
} satisfies RouteConfigsTable;
