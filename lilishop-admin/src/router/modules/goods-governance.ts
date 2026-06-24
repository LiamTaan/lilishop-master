import { goodsGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/goods-governance",
  component: Layout,
  redirect: "/goods-governance/goods-manage",
  meta: {
    icon: "ep:goods",
    title: "商品管理",
    rank: goodsGovernance
  },
  children: [
    {
      path: "/goods-governance/goods-manage",
      name: "GoodsManage",
      component: () => import("@/views/goods-governance/goods-manage/index.vue"),
      meta: { title: "商品列表" }
    },
    {
      path: "/goods-governance/brand-manage",
      name: "BrandManage",
      component: () => import("@/views/goods-governance/brand-manage/index.vue"),
      meta: { title: "品牌管理" }
    },
    {
      path: "/goods-governance/parameter-manage",
      name: "ParameterManage",
      component: () =>
        import("@/views/goods-governance/parameter-manage/index.vue"),
      meta: { title: "参数管理" }
    },
    {
      path: "/goods-governance/category-manage",
      name: "CategoryManage",
      component: () =>
        import("@/views/goods-governance/category-manage/index.vue"),
      meta: { title: "分类管理" }
    },
    {
      path: "/goods-governance/goods-group-manage",
      name: "GoodsGroupManage",
      component: () =>
        import("@/views/goods-governance/goods-group-manage/index.vue"),
      meta: { title: "商品分组" }
    },
    {
      path: "/goods-governance/home-category",
      name: "HomeCategoryManage",
      component: () => import("@/views/goods-governance/home-category/index.vue"),
      meta: { title: "首页分类配置", showLink: false }
    },
    {
      path: "/goods-governance/points-goods",
      name: "PointsGoodsManage",
      component: () => import("@/views/goods-governance/points-goods/index.vue"),
      meta: { title: "积分商品治理", showLink: false }
    },
    {
      path: "/goods-governance/card-coupon-goods",
      name: "CardCouponGoodsManage",
      component: () =>
        import("@/views/goods-governance/card-coupon-goods/index.vue"),
      meta: { title: "卡券商品治理", showLink: false }
    }
  ]
} satisfies RouteConfigsTable;
