import { contentCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/content-center",
  component: Layout,
  redirect: "/content-center/advertisement",
  meta: {
    icon: "ep:document",
    title: "内容与运营",
    rank: contentCenter
  },
  children: [
    {
      path: "/content-center/advertisement",
      name: "ContentAdvertisement",
      component: () =>
        import("@/views/super-admin/content-center/advertisement.vue"),
      meta: { title: "广告位管理" }
    },
    {
      path: "/content-center/special-manage",
      name: "ContentSpecialManage",
      component: () =>
        import("@/views/super-admin/content-center/special-manage.vue"),
      meta: { title: "专题管理" }
    },
    {
      path: "/content-center/recommendation-strategy",
      name: "ContentRecommendationStrategy",
      component: () =>
        import("@/views/super-admin/content-center/recommendation-strategy.vue"),
      meta: { title: "推荐策略" }
    },
    {
      path: "/content-center/article",
      name: "ContentArticle",
      component: () => import("@/views/super-admin/content-center/article.vue"),
      meta: { title: "帮助与公告" }
    },
    {
      path: "/content-center/article-category",
      name: "ContentArticleCategory",
      component: () =>
        import("@/views/super-admin/content-center/article-category.vue"),
      meta: { title: "帮助分类" }
    },
    {
      path: "/content-center/sensitive-words",
      name: "ContentSensitiveWords",
      component: () =>
        import("@/views/super-admin/content-center/sensitive-words.vue"),
      meta: { title: "敏感词治理" }
    },
    {
      path: "/content-center/custom-words",
      name: "ContentCustomWords",
      component: () =>
        import("@/views/super-admin/content-center/custom-words.vue"),
      meta: { title: "自定义词库" }
    },
    {
      path: "/content-center/app-version",
      name: "ContentAppVersion",
      component: () => import("@/views/super-admin/content-center/app-version.vue"),
      meta: { title: "APP版本" }
    }
  ]
} satisfies RouteConfigsTable;
