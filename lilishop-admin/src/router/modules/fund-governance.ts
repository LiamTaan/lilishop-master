import { fundGovernance } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/fund-governance",
  component: Layout,
  redirect: "/fund-governance/profitsharing-rule",
  meta: {
    icon: "ep:money",
    title: "分账管理",
    rank: fundGovernance
  },
  children: [
    {
      path: "/fund-governance/profitsharing-rule",
      name: "ProfitSharingRule",
      component: () =>
        import("@/views/fund-governance/profitsharing-rule/index.vue"),
      meta: { title: "分账规则" }
    },
    {
      path: "/fund-governance/bill-manage",
      name: "BillManage",
      component: () => import("@/views/fund-governance/bill-manage/index.vue"),
      meta: { title: "结算单" }
    },
    {
      path: "/fund-governance/profitsharing-record",
      name: "ProfitSharingRecord",
      component: () =>
        import("@/views/fund-governance/profitsharing-record/index.vue"),
      meta: { title: "分账明细" }
    },
    {
      path: "/fund-governance/wallet-log",
      name: "WalletLog",
      component: () => import("@/views/fund-governance/wallet-log/index.vue"),
      meta: { title: "余额记录" }
    },
    {
      path: "/fund-governance/profitsharing-balance",
      name: "ProfitSharingBalance",
      component: () =>
        import("@/views/fund-governance/profitsharing-balance/index.vue"),
      meta: { title: "分账治理概览", showLink: false }
    },
    {
      path: "/fund-governance/wallet-account",
      name: "WalletAccount",
      component: () => import("@/views/fund-governance/wallet-account/index.vue"),
      meta: { title: "钱包账户台账", showLink: false }
    },
    {
      path: "/fund-governance/withdraw-apply",
      name: "WithdrawApply",
      component: () => import("@/views/fund-governance/withdraw-apply/index.vue"),
      meta: { title: "提现审核", showLink: false }
    },
    {
      path: "/fund-governance/procurement-reconciliation",
      name: "ProcurementReconciliation",
      component: () =>
        import("@/views/fund-governance/procurement-reconciliation/index.vue"),
      meta: { title: "采购对账", showLink: false }
    },
    {
      path: "/fund-governance/fund-reconciliation",
      name: "FundReconciliation",
      component: () =>
        import("@/views/fund-governance/fund-reconciliation/index.vue"),
      meta: { title: "资金对账", showLink: false }
    }
  ]
} satisfies RouteConfigsTable;
