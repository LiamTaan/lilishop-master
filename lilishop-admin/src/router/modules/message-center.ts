import { messageCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/message-center",
  component: Layout,
  redirect: "/message-center/notice-center",
  meta: {
    icon: "ep:chat-dot-round",
    title: "通知与消息",
    rank: messageCenter
  },
  children: [
    {
      path: "/message-center/notice-center",
      name: "NoticeCenter",
      component: () => import("@/views/super-admin/message-center/notice-center.vue"),
      meta: { title: "站内通知" }
    }
  ]
} satisfies RouteConfigsTable;
