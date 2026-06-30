import { memberCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/member-center",
  component: Layout,
  redirect: "/member-center/member-list",
  meta: {
    icon: "ep:user",
    title: "用户中心",
    rank: memberCenter
  },
  children: [
    {
      path: "/member-center/member-list",
      name: "MemberList",
      component: () => import("@/views/super-admin/member-center/member-list.vue"),
      meta: { title: "用户账号管理" }
    },
    {
      path: "/member-center/member-group",
      name: "MemberGroup",
      component: () => import("@/views/super-admin/member-center/member-group.vue"),
      meta: { title: "会员分组" }
    },
    {
      path: "/member-center/member-benefit",
      name: "MemberBenefit",
      component: () => import("@/views/super-admin/member-center/member-benefit.vue"),
      meta: { title: "会员权益" }
    },
    {
      path: "/member-center/member-points-history",
      name: "MemberPointsHistory",
      component: () => import("@/views/super-admin/member-center/member-points-history.vue"),
      meta: { title: "积分记录" }
    },
    {
      path: "/member-center/member-evaluation",
      name: "MemberEvaluation",
      component: () => import("@/views/super-admin/member-center/member-evaluation.vue"),
      meta: { title: "用户评价" }
    },
    {
      path: "/member-center/member-address",
      name: "MemberAddress",
      component: () => import("@/views/super-admin/member-center/member-address.vue"),
      meta: { title: "用户地址" }
    }
  ]
} satisfies RouteConfigsTable;
