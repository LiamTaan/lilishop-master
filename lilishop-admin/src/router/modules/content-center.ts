import { contentCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/content-center",
  component: Layout,
  redirect: "/content-center/home-config",
  meta: {
    icon: "ep:document",
    title: "内容与运营",
    rank: contentCenter
  },
  children: [
    {
      path: "/content-center/home-config",
      name: "ContentHomeConfig",
      component: () =>
        import("@/views/super-admin/content-center/home-config.vue"),
      meta: { title: "首页配置" }
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
      meta: { title: "帮助分类", showLink: false }
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
      meta: { title: "自定义词库", showLink: false }
    },
    {
      path: "/content-center/hot-words-manage",
      name: "ContentHotWordsManage",
      component: () =>
        import("@/views/super-admin/content-center/hot-words-manage.vue"),
      meta: { title: "搜索治理" }
    },
    {
      path: "/content-center/app-version",
      name: "ContentAppVersion",
      component: () => import("@/views/super-admin/content-center/app-version.vue"),
      meta: { title: "APP版本", showLink: false }
    }
  ]
} satisfies RouteConfigsTable;
