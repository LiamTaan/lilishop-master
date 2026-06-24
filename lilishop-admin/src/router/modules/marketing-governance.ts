import { marketingGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/marketing-governance",
  component: Layout,
  redirect: "/marketing-governance/coupon-manage",
  meta: {
    icon: "ep:present",
    title: "营销管理",
    rank: marketingGovernance
  },
  children: [
    {
      path: "/marketing-governance/coupon-manage",
      name: "CouponManage",
      component: () =>
        import("@/views/marketing-governance/coupon-manage/index.vue"),
      meta: { title: "优惠券管理" }
    },
    {
      path: "/marketing-governance/coupon-activity",
      name: "CouponActivityManage",
      component: () =>
        import("@/views/marketing-governance/coupon-activity/index.vue"),
      meta: { title: "券活动管理" }
    },
    {
      path: "/marketing-governance/full-discount",
      name: "FullDiscountManage",
      component: () =>
        import("@/views/marketing-governance/full-discount/index.vue"),
      meta: { title: "满减活动管理" }
    },
    {
      path: "/marketing-governance/seckill-manage",
      name: "SeckillManage",
      component: () =>
        import("@/views/marketing-governance/seckill-manage/index.vue"),
      meta: { title: "秒杀活动管理" }
    },
    {
      path: "/marketing-governance/kanjia-manage",
      name: "KanjiaManage",
      component: () =>
        import("@/views/marketing-governance/kanjia-manage/index.vue"),
      meta: { title: "砍价活动管理" }
    },
    {
      path: "/marketing-governance/pintuan-rule",
      name: "PintuanRule",
      component: () =>
        import("@/views/marketing-governance/pintuan-manage/index.vue"),
      meta: { title: "拼团规则" }
    },
    {
      path: "/marketing-governance/pintuan-record",
      name: "PintuanRecord",
      component: () =>
        import("@/views/marketing-governance/pintuan-record/index.vue"),
      meta: { title: "拼团记录" }
    }
  ]
} satisfies RouteConfigsTable;
