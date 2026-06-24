import { messageCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/message-center",
  component: Layout,
  redirect: "/message-center/member-notice",
  meta: {
    icon: "ep:chat-dot-round",
    title: "通知与消息",
    rank: messageCenter
  },
  children: [
    {
      path: "/message-center/member-notice",
      name: "MemberNotice",
      component: () => import("@/views/super-admin/message-center/member-notice.vue"),
      meta: { title: "站内通知" }
    },
    {
      path: "/message-center/member-notice-log",
      name: "MemberNoticeLog",
      component: () => import("@/views/super-admin/message-center/member-notice-log.vue"),
      meta: { title: "通知日志" }
    },
    {
      path: "/message-center/sms-sign",
      name: "SmsSign",
      component: () => import("@/views/super-admin/message-center/sms-sign.vue"),
      meta: { title: "短信签名" }
    },
    {
      path: "/message-center/sms-template",
      name: "SmsTemplate",
      component: () => import("@/views/super-admin/message-center/sms-template.vue"),
      meta: { title: "短信模板" }
    },
    {
      path: "/message-center/member-notice-sender",
      name: "MemberNoticeSender",
      component: () =>
        import("@/views/super-admin/message-center/member-notice-sender.vue"),
      meta: { title: "发送任务" }
    },
    {
      path: "/message-center/service-notice",
      name: "ServiceNotice",
      component: () =>
        import("@/views/super-admin/message-center/service-notice.vue"),
      meta: { title: "服务通知" }
    },
    {
      path: "/message-center/message-channel",
      name: "MessageChannel",
      component: () =>
        import("@/views/super-admin/message-center/message-channel.vue"),
      meta: { title: "消息发送管理" }
    },
    {
      path: "/message-center/member-message",
      name: "MemberMessage",
      component: () =>
        import("@/views/super-admin/message-center/member-message.vue"),
      meta: { title: "客户消息" }
    },
    {
      path: "/message-center/store-message",
      name: "StoreMessage",
      component: () =>
        import("@/views/super-admin/message-center/store-message.vue"),
      meta: { title: "店铺消息" }
    }
  ]
} satisfies RouteConfigsTable;
