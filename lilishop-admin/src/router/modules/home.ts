import { home } from "@/router/enums";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/",
  name: "Home",
  component: Layout,
  redirect: "/welcome",
  meta: {
    icon: "ep:data-board",
    title: "工作台",
    rank: home
  },
  children: [
    {
      path: "/welcome",
      name: "WholesaleDashboardHome",
      component: () => import("@/views/dashboard/wholesale/index.vue"),
      meta: {
        title: "工作台"
      }
    }
  ]
} satisfies RouteConfigsTable;
