import { dashboard } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/dashboard",
  component: Layout,
  redirect: "/dashboard/overview",
  meta: {
    icon: "ep:data-board",
    title: "数据概览",
    rank: dashboard
  },
  children: [
    {
      path: "/dashboard/wholesale",
      name: "WholesaleDashboard",
      component: () => import("@/views/dashboard/wholesale/index.vue"),
      meta: {
        title: "工作台",
        showLink: false
      }
    },
    {
      path: "/dashboard/overview",
      name: "PlatformOverview",
      component: () => import("@/views/dashboard/overview/index.vue"),
      meta: {
        title: "数据概览"
      }
    },
    {
      path: "/dashboard/store-rank",
      name: "StoreRank",
      component: () => import("@/views/dashboard/store-rank/index.vue"),
      meta: {
        title: "门店排行",
        showLink: false
      }
    },
    {
      path: "/dashboard/goods-rank",
      name: "GoodsRank",
      component: () => import("@/views/dashboard/goods-rank/index.vue"),
      meta: {
        title: "商品/品类排行",
        showLink: false
      }
    },
    {
      path: "/dashboard/order-overview",
      name: "OrderOverview",
      component: () => import("@/views/dashboard/order-overview/index.vue"),
      meta: {
        title: "订单概览",
        showLink: false
      }
    }
  ]
} satisfies RouteConfigsTable;
