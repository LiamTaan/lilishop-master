import { permission } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/permission",
  component: Layout,
  redirect: "/permission/page",
  meta: {
    icon: "ep:key",
    title: "权限中心",
    rank: permission
  },
  children: [
    {
      path: "/permission/page",
      name: "PermissionPage",
      component: () => import("@/views/permission/page/index.vue"),
      meta: { title: "页面权限验证" }
    },
    {
      path: "/permission/button",
      name: "PermissionButton",
      component: () => import("@/views/permission/button/index.vue"),
      meta: { title: "按钮权限验证" }
    }
  ]
} satisfies RouteConfigsTable;
