import { storeGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/store-governance",
  component: Layout,
  redirect: "/store-governance/store-manage",
  meta: {
    icon: "ep:shop",
    title: "店铺治理",
    rank: storeGovernance
  },
  children: [
    {
      path: "/store-governance/store-apply",
      name: "StoreApplyAudit",
      component: () => import("@/views/store-governance/store-apply/index.vue"),
      meta: {
        title: "入驻审核",
        showLink: false
      }
    },
    {
      path: "/store-governance/store-manage",
      name: "StoreManage",
      component: () =>
        import("@/views/store-governance/store-manage/index.vue"),
      meta: {
        title: "店铺管理"
      }
    },
    {
      path: "/store-governance/agent-manage",
      name: "AgentManage",
      component: () =>
        import("@/views/store-governance/agent-manage/index.vue"),
      meta: {
        title: "代理商治理",
        showLink: false
      }
    },
    {
      path: "/store-governance/store-audit-log",
      name: "StoreAuditLog",
      component: () =>
        import("@/views/store-governance/store-audit-log/index.vue"),
      meta: {
        title: "店铺审核历史",
        showLink: false
      }
    }
  ]
} satisfies RouteConfigsTable;
