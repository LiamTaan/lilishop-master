import { system } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/system",
  component: Layout,
  redirect: "/system/warning-setting",
  meta: {
    icon: "ep:setting",
    title: "系统设置",
    rank: system
  },
  children: [
    {
      path: "/system/warning-setting",
      name: "SystemWarningSetting",
      component: () =>
        import("@/views/super-admin/message-center/service-notice.vue"),
      meta: { title: "预警设置" }
    },
    {
      path: "/system/maintenance-log",
      name: "SystemMaintenanceLog",
      component: () =>
        import("@/views/super-admin/support-center/setting-log.vue"),
      meta: { title: "系统维护记录" }
    },
    {
      path: "/system/operation-log",
      name: "SystemOperationLog",
      component: () => import("@/views/monitor/logs/operation/index.vue"),
      meta: { title: "操作日志" }
    },
    {
      path: "/system/user",
      name: "SystemUser",
      component: () => import("@/views/system/user/index.vue"),
      meta: { title: "平台账号台账", showLink: false }
    },
    {
      path: "/system/role",
      name: "SystemRole",
      component: () => import("@/views/system/role/index.vue"),
      meta: { title: "角色权限配置", showLink: false }
    },
    {
      path: "/system/dept",
      name: "SystemDept",
      component: () => import("@/views/system/dept/index.vue"),
      meta: { title: "组织架构配置", showLink: false }
    },
    {
      path: "/system/menu",
      name: "SystemMenu",
      component: () => import("@/views/system/menu/index.vue"),
      meta: { title: "菜单权限配置", showLink: false }
    }
  ]
} satisfies RouteConfigsTable;
