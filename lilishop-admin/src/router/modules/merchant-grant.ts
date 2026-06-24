import { merchantGrant } from "@/router/enums";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/merchant-grant",
  component: Layout,
  redirect: "/merchant-grant/menu-permission",
  meta: {
    icon: "ep:key",
    title: "系统与权限",
    rank: merchantGrant
  },
  children: [
    {
      path: "/merchant-grant/menu-permission",
      name: "MerchantGrantMenuPermission",
      component: () => import("@/views/system/menu/index.vue"),
      meta: { title: "平台菜单权限" }
    },
    {
      path: "/merchant-grant/role-permission",
      name: "MerchantGrantRolePermission",
      component: () => import("@/views/system/role/index.vue"),
      meta: { title: "平台角色权限" }
    },
    {
      path: "/merchant-grant/department-manage",
      name: "MerchantGrantDepartmentManage",
      component: () => import("@/views/system/dept/index.vue"),
      meta: { title: "组织架构配置" }
    },
    {
      path: "/merchant-grant/backend-account",
      name: "MerchantGrantBackendAccount",
      component: () => import("@/views/system/user/index.vue"),
      meta: { title: "平台后台账号" }
    },
    {
      path: "/merchant-grant/store-menu",
      name: "MerchantGrantStoreMenu",
      component: () => import("@/views/super-admin/merchant-grant/index.vue"),
      meta: { title: "店铺菜单资源" }
    }
  ]
} satisfies RouteConfigsTable;
