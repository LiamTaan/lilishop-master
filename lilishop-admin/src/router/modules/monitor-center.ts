import { monitor } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/monitor",
  component: Layout,
  redirect: "/monitor/online",
  meta: {
    icon: "ep:monitor",
    title: "平台监控",
    rank: monitor
  },
  children: [
    {
      path: "/monitor/online",
      name: "MonitorOnline",
      component: () => import("@/views/monitor/online/index.vue"),
      meta: { title: "在线会话台账" }
    },
    {
      path: "/monitor/logs/login",
      name: "MonitorLoginLog",
      component: () => import("@/views/monitor/logs/login/index.vue"),
      meta: { title: "登录行为台账" }
    },
    {
      path: "/monitor/logs/operation",
      name: "MonitorOperationLog",
      component: () => import("@/views/monitor/logs/operation/index.vue"),
      meta: { title: "操作留痕台账" }
    },
    {
      path: "/monitor/logs/system",
      name: "MonitorSystemLog",
      component: () => import("@/views/monitor/logs/system/index.vue"),
      meta: { title: "系统请求台账" }
    }
  ]
} satisfies RouteConfigsTable;
