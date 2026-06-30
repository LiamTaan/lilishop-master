import { supportCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/support-center",
  component: Layout,
  redirect: "/support-center/verification-source",
  meta: {
    icon: "ep:setting",
    title: "系统支撑",
    rank: supportCenter
  },
  children: [
    {
      path: "/support-center/verification-source",
      name: "SupportVerificationSource",
      component: () =>
        import("@/views/super-admin/support-center/verification-source.vue"),
      meta: { title: "验证码资源" }
    },
    {
      path: "/support-center/region-manage",
      name: "SupportRegionManage",
      component: () =>
        import("@/views/super-admin/support-center/region-manage.vue"),
      meta: { title: "区域管理" }
    },
    {
      path: "/support-center/logistics-manage",
      name: "SupportLogisticsManage",
      component: () =>
        import("@/views/super-admin/support-center/logistics-manage.vue"),
      meta: { title: "物流公司" }
    },
    {
      path: "/support-center/freight-template-manage",
      name: "SupportFreightTemplateManage",
      component: () =>
        import("@/views/super-admin/support-center/freight-template-manage.vue"),
      meta: { title: "运费模板" }
    },
    {
      path: "/support-center/after-sale-reason",
      name: "SupportAfterSaleReason",
      component: () =>
        import("@/views/super-admin/support-center/after-sale-reason.vue"),
      meta: { title: "售后原因" }
    },
    {
      path: "/support-center/feedback-manage",
      name: "SupportFeedbackManage",
      component: () =>
        import("@/views/super-admin/support-center/feedback-manage.vue"),
      meta: { title: "意见反馈" }
    },
    {
      path: "/support-center/setting-log",
      name: "SupportSettingLog",
      component: () =>
        import("@/views/super-admin/support-center/setting-log.vue"),
      meta: { title: "配置日志" }
    }
  ]
} satisfies RouteConfigsTable;
