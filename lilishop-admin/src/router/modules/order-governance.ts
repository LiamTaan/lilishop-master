import { orderGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/order-governance",
  component: Layout,
  redirect: "/order-governance/order-manage",
  meta: {
    icon: "ep:document",
    title: "订单管理",
    rank: orderGovernance
  },
  children: [
    {
      path: "/order-governance/order-manage",
      name: "OrderManage",
      component: () => import("@/views/order-governance/order-manage/index.vue"),
      meta: { title: "订单列表" }
    },
    {
      path: "/order-governance/order-complaint",
      name: "OrderComplaintManage",
      component: () =>
        import("@/views/order-governance/order-complaint/index.vue"),
      meta: { title: "订单投诉" }
    },
    {
      path: "/order-governance/verification-rule",
      name: "VerificationRuleManage",
      component: () =>
        import("@/views/order-governance/verification-rule/index.vue"),
      meta: { title: "核销规则" }
    },
    {
      path: "/order-governance/refund-log",
      name: "RefundLogManage",
      component: () => import("@/views/order-governance/refund-log/index.vue"),
      meta: { title: "退款日志" }
    },
    {
      path: "/order-governance/payment-log",
      name: "PaymentLogManage",
      component: () => import("@/views/order-governance/payment-log/index.vue"),
      meta: { title: "支付日志" }
    },
    {
      path: "/order-governance/receipt-manage",
      name: "ReceiptManage",
      component: () =>
        import("@/views/order-governance/receipt-manage/index.vue"),
      meta: { title: "发票管理" }
    },
    {
      path: "/order-governance/order-log",
      name: "OrderLogManage",
      component: () => import("@/views/order-governance/order-log/index.vue"),
      meta: { title: "订单日志" }
    },
    {
      path: "/order-governance/after-sale",
      name: "AfterSaleManage",
      component: () => import("@/views/order-governance/after-sale/index.vue"),
      meta: { title: "售后治理", showLink: false }
    },
    {
      path: "/order-governance/verification-record",
      name: "VerificationRecordManage",
      component: () =>
        import("@/views/order-governance/verification-record/index.vue"),
      meta: { title: "核销记录" }
    }
  ]
} satisfies RouteConfigsTable;
