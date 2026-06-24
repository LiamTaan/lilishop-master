import { contentCenter } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/content-center",
  component: Layout,
  redirect: "/content-center/article",
  meta: {
    icon: "ep:document",
    title: "内容管理",
    rank: contentCenter
  },
  children: [
    {
      path: "/content-center/article",
      name: "ContentArticle",
      component: () => import("@/views/super-admin/content-center/article.vue"),
      meta: { title: "公告管理" }
    },
    {
      path: "/content-center/page-data",
      name: "ContentPageData",
      component: () => import("@/views/super-admin/content-center/page-data.vue"),
      meta: { title: "页面装修", showLink: false }
    },
    {
      path: "/content-center/shortcut-nav",
      name: "ContentShortcutNav",
      component: () =>
        import("@/views/super-admin/content-center/shortcut-nav.vue"),
      meta: { title: "快捷导航", showLink: false }
    },
    {
      path: "/content-center/article-category",
      name: "ContentArticleCategory",
      component: () =>
        import("@/views/super-admin/content-center/article-category.vue"),
      meta: { title: "文章分类", showLink: false }
    },
    {
      path: "/content-center/sensitive-words",
      name: "ContentSensitiveWords",
      component: () =>
        import("@/views/super-admin/content-center/sensitive-words.vue"),
      meta: { title: "敏感词管理", showLink: false }
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
      meta: { title: "热词治理", showLink: false }
    },
    {
      path: "/content-center/special",
      name: "ContentSpecial",
      component: () => import("@/views/super-admin/content-center/special.vue"),
      meta: { title: "专题管理", showLink: false }
    },
    {
      path: "/content-center/app-version",
      name: "ContentAppVersion",
      component: () => import("@/views/super-admin/content-center/app-version.vue"),
      meta: { title: "APP版本", showLink: false }
    }
  ]
} satisfies RouteConfigsTable;
