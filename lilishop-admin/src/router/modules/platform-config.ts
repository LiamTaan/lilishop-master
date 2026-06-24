import { platformConfig } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/platform-config",
  component: Layout,
  redirect: "/platform-config/base-setting",
  meta: {
    icon: "ep:tools",
    title: "平台配置",
    rank: platformConfig
  },
  children: [
    {
      path: "/platform-config/base-setting",
      name: "PlatformBaseSetting",
      component: () =>
        import("@/views/super-admin/platform-config/base-setting.vue"),
      meta: { title: "基础设置" }
    },
    {
      path: "/platform-config/payment-setting",
      name: "PlatformPaymentSetting",
      component: () =>
        import("@/views/super-admin/platform-config/payment-setting.vue"),
      meta: { title: "支付配置" }
    },
    {
      path: "/platform-config/withdraw-setting",
      name: "PlatformWithdrawSetting",
      component: () =>
        import("@/views/super-admin/platform-config/withdraw-setting.vue"),
      meta: { title: "提现设置" }
    },
    {
      path: "/platform-config/oss-setting",
      name: "PlatformOssSetting",
      component: () =>
        import("@/views/super-admin/platform-config/oss-setting.vue"),
      meta: { title: "对象存储配置" }
    },
    {
      path: "/platform-config/sms-setting",
      name: "PlatformSmsSetting",
      component: () =>
        import("@/views/super-admin/platform-config/sms-setting.vue"),
      meta: { title: "短信配置" }
    },
    {
      path: "/platform-config/email-setting",
      name: "PlatformEmailSetting",
      component: () =>
        import("@/views/super-admin/platform-config/email-setting.vue"),
      meta: { title: "邮件配置" }
    },
    {
      path: "/platform-config/goods-setting",
      name: "PlatformGoodsSetting",
      component: () =>
        import("@/views/super-admin/platform-config/goods-setting.vue"),
      meta: { title: "商品设置" }
    },
    {
      path: "/platform-config/order-setting",
      name: "PlatformOrderSetting",
      component: () =>
        import("@/views/super-admin/platform-config/order-setting.vue"),
      meta: { title: "订单设置" }
    },
    {
      path: "/platform-config/logistics-setting",
      name: "PlatformLogisticsSetting",
      component: () =>
        import("@/views/super-admin/platform-config/logistics-setting.vue"),
      meta: { title: "物流设置" }
    },
    {
      path: "/platform-config/point-setting",
      name: "PlatformPointSetting",
      component: () =>
        import("@/views/super-admin/platform-config/point-setting.vue"),
      meta: { title: "积分设置" }
    },
    {
      path: "/platform-config/experience-setting",
      name: "PlatformExperienceSetting",
      component: () =>
        import("@/views/super-admin/platform-config/experience-setting.vue"),
      meta: { title: "经验值设置" }
    },
    {
      path: "/platform-config/seckill-setting",
      name: "PlatformSeckillSetting",
      component: () =>
        import("@/views/super-admin/platform-config/seckill-setting.vue"),
      meta: { title: "秒杀设置" }
    },
    {
      path: "/platform-config/im-setting",
      name: "PlatformImSetting",
      component: () =>
        import("@/views/super-admin/platform-config/im-setting.vue"),
      meta: { title: "IM配置" }
    },
    {
      path: "/platform-config/connect-setting",
      name: "PlatformConnectSetting",
      component: () =>
        import("@/views/super-admin/platform-config/connect-setting.vue"),
      meta: { title: "登录设置" }
    },
    {
      path: "/platform-config/distribution-setting",
      name: "PlatformDistributionSetting",
      component: () =>
        import("@/views/super-admin/platform-config/distribution-setting.vue"),
      meta: { title: "分销设置" }
    },
    {
      path: "/platform-config/alipay-setting",
      name: "PlatformAlipaySetting",
      component: () =>
        import("@/views/super-admin/platform-config/alipay-setting.vue"),
      meta: { title: "支付宝配置" }
    },
    {
      path: "/platform-config/wechat-payment-setting",
      name: "PlatformWechatPaymentSetting",
      component: () =>
        import("@/views/super-admin/platform-config/wechat-payment-setting.vue"),
      meta: { title: "微信支付配置" }
    },
    {
      path: "/platform-config/unionpay-setting",
      name: "PlatformUnionpaySetting",
      component: () =>
        import("@/views/super-admin/platform-config/unionpay-setting.vue"),
      meta: { title: "银联支付配置" }
    },
    {
      path: "/platform-config/wechat-connect-setting",
      name: "PlatformWechatConnectSetting",
      component: () =>
        import("@/views/super-admin/platform-config/wechat-connect-setting.vue"),
      meta: { title: "微信登录配置" }
    },
    {
      path: "/platform-config/qq-connect-setting",
      name: "PlatformQqConnectSetting",
      component: () =>
        import("@/views/super-admin/platform-config/qq-connect-setting.vue"),
      meta: { title: "QQ登录配置" }
    },
    {
      path: "/platform-config/hot-words-setting",
      name: "PlatformHotWordsSetting",
      component: () =>
        import("@/views/super-admin/platform-config/hot-words-setting.vue"),
      meta: { title: "热词设置" }
    },
    {
      path: "/platform-config/wx-channels-setting",
      name: "PlatformWxChannelsSetting",
      component: () =>
        import("@/views/super-admin/platform-config/wx-channels-setting.vue"),
      meta: { title: "视频号配置" }
    }
  ]
} satisfies RouteConfigsTable;
