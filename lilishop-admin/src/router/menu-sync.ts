import { cloneDeep } from "@pureadmin/utils";
import { businessRouteModules } from "./business-routes";

type BackendMenuNode = Record<string, any> & {
  children?: BackendMenuNode[];
};

type CurrentRouteNode = RouteConfigsTable & {
  children?: CurrentRouteNode[];
};

export type BackendMenuBinding = {
  currentPath: string;
  currentTitle: string;
  currentParentTitle: string;
  currentName: string;
  visibleInSidebar: boolean;
};

type FlatRouteRecord = {
  path: string;
  name: string;
  title: string;
  parentPath: string;
  parentTitle: string;
  visibleInSidebar: boolean;
};

const backendMenuAliasMap: Record<string, string> = {
  "statistics/wholesale-dashboard/index": "/dashboard/overview",
  "wholesale-dashboard": "/dashboard/overview",
  "platform-overview": "/dashboard/overview",
  "store-manage": "/store-governance/store-manage",
  "store-apply": "/store-governance/store-apply",
  "seller/store/store-apply/index": "/store-governance/store-apply",
  "store-audit-log": "/store-governance/store-audit-log",
  "agent-manage": "/store-governance/agent-manage",
  "seller/agent/agent-manage": "/store-governance/agent-manage",
  "goods-manage": "/goods-governance/goods-manage",
  "brand-manage": "/goods-governance/brand-manage",
  "goods/brand/index": "/goods-governance/brand-manage",
  "promotions/brand/brand": "/goods-governance/brand-manage",
  "parameter-manage": "/goods-governance/parameter-manage",
  "goods/parameters/index": "/goods-governance/parameter-manage",
  "goods-category": "/goods-governance/category-manage",
  "goods-group": "/goods-governance/goods-group-manage",
  "goods-group-manage": "/goods-governance/goods-group-manage",
  "goods/goods-group/index": "/goods-governance/goods-group-manage",
  "point-goods": "/goods-governance/points-goods",
  "card-coupon-goods": "/goods-governance/card-coupon-goods",
  "promotions/card-coupon-goods/index": "/goods-governance/card-coupon-goods",
  "order-list": "/order-governance/order-manage",
  "order-complaint": "/order-governance/order-complaint",
  "order/complain/index": "/order-governance/order-complaint",
  "refund-log": "/order-governance/refund-log",
  "order/refund-log/index": "/order-governance/refund-log",
  "payment-log": "/order-governance/payment-log",
  "order/payment-log/index": "/order-governance/payment-log",
  "receipt-manage": "/order-governance/receipt-manage",
  "trade/receipt/index": "/order-governance/receipt-manage",
  "order-log": "/order-governance/order-log",
  "order/order-log/index": "/order-governance/order-log",
  "verification-manage": "/order-governance/verification-rule",
  "verification-record": "/order-governance/verification-record",
  "order/verification-record/index": "/order-governance/verification-record",
  "after-sale-manage": "/order-governance/after-sale",
  coupon: "/marketing-governance/coupon-manage",
  "promotion/coupon/coupon": "/marketing-governance/coupon-manage",
  "promotions/coupon/coupon": "/marketing-governance/coupon-manage",
  "coupon-activity": "/marketing-governance/coupon-activity",
  "promotion/couponactivity/coupon": "/marketing-governance/coupon-activity",
  "promotion/coupon-activity/coupon": "/marketing-governance/coupon-activity",
  "promotions/coupon-activity": "/marketing-governance/coupon-activity",
  "full-discount": "/marketing-governance/full-discount",
  "promotion/full-discount/full-discount":
    "/marketing-governance/full-discount",
  "promotions/full-discount": "/marketing-governance/full-discount",
  seckill: "/marketing-governance/seckill-manage",
  "promotion/seckill/seckill": "/marketing-governance/seckill-manage",
  "promotions/seckill": "/marketing-governance/seckill-manage",
  kanjia: "/marketing-governance/kanjia-manage",
  "promotions/kanjia": "/marketing-governance/kanjia-manage",
  "promotions/kanjia/kanjia-activity-goods":
    "/marketing-governance/kanjia-manage",
  "pintuan-manage": "/marketing-governance/pintuan-rule",
  "pintuan-record": "/marketing-governance/pintuan-record",
  "bill-manage": "/fund-governance/bill-manage",
  "order/bill/index": "/fund-governance/bill-manage",
  "profitsharing-rule": "/fund-governance/profitsharing-rule",
  "finance/profitsharing-rule/index": "/fund-governance/profitsharing-rule",
  "profitsharing-record": "/fund-governance/profitsharing-record",
  "finance/profitsharing-record/index": "/fund-governance/profitsharing-record",
  "wallet-log": "/fund-governance/wallet-log",
  "profitsharing-balance": "/fund-governance/profitsharing-balance",
  "finance/profitsharing-balance/index":
    "/fund-governance/profitsharing-balance",
  "wallet-account": "/fund-governance/wallet-account",
  "finance/wallet-account/index": "/fund-governance/wallet-account",
  "withdraw-apply": "/fund-governance/withdraw-apply",
  "finance/withdraw-apply/index": "/fund-governance/withdraw-apply",
  "procurement-reconciliation": "/fund-governance/procurement-reconciliation",
  "finance/procurement-reconciliation/index":
    "/fund-governance/procurement-reconciliation",
  "fund-reconciliation": "/fund-governance/fund-reconciliation",
  "finance/fund-reconciliation/index": "/fund-governance/fund-reconciliation",
  "procurement-governance": "/procurement-governance/order",
  "procurement/order/index": "/procurement-governance/order",
  "procurement-order": "/procurement-governance/order",
  "manager/procurement/order": "/procurement-governance/order",
  "procurement/inbound/index": "/procurement-governance/inbound",
  "procurement-inbound": "/procurement-governance/inbound",
  "manager/procurement/inbound": "/procurement-governance/inbound",
  "procurement/inventory-count/index":
    "/procurement-governance/inventory-count",
  "procurement-inventory-count": "/procurement-governance/inventory-count",
  "manager/procurement/inventory-count":
    "/procurement-governance/inventory-count",
  "procurement/damage-report/index": "/procurement-governance/damage-report",
  "procurement-damage-report": "/procurement-governance/damage-report",
  "manager/procurement/damage-report": "/procurement-governance/damage-report",
  "procurement/reason/index": "/procurement-governance/reason",
  "procurement-reason": "/procurement-governance/reason",
  "manager/procurement/reason": "/procurement-governance/reason",
  "member-list": "/member-center/member-list",
  "member-group": "/member-center/member-group",
  "member-benefit": "/member-center/member-benefit",
  "member-points-history": "/member-center/member-points-history",
  "member-evaluation": "/member-center/member-evaluation",
  "member-address": "/member-center/member-address",
  member: "/member-center/member-list",
  notice: "/message-center/member-notice",
  "member-notice": "/message-center/member-notice",
  "member-notice-log": "/message-center/member-notice-log",
  "sms-sign": "/message-center/sms-sign",
  "sms-template": "/message-center/sms-template",
  "member-notice-sender": "/message-center/member-notice-sender",
  "service-notice": "/message-center/service-notice",
  "message-channel": "/message-center/message-channel",
  "member-message": "/message-center/member-message",
  "store-message": "/message-center/store-message",
  article: "/content-center/article",
  advertisement: "/content-center/advertisement",
  advert: "/content-center/advertisement",
  banner: "/content-center/advertisement",
  "home-advertisement": "/content-center/advertisement",
  special: "/content-center/special-manage",
  "content-center/special": "/content-center/special-manage",
  "special-manage": "/content-center/special-manage",
  recommendation: "/content-center/recommendation-strategy",
  "recommendation-strategy": "/content-center/recommendation-strategy",
  "article-category": "/content-center/article-category",
  "sensitive-words": "/content-center/sensitive-words",
  "custom-words": "/content-center/custom-words",
  "app-version": "/content-center/app-version",
  "verification-source": "/support-center/verification-source",
  "region-manage": "/support-center/region-manage",
  "logistics-manage": "/support-center/logistics-manage",
  "freight-template-manage": "/support-center/freight-template-manage",
  "after-sale-reason": "/support-center/after-sale-reason",
  "feedback-manage": "/support-center/feedback-manage",
  "setting-log": "/support-center/setting-log",
  "warning-setting": "/system/warning-setting",
  "maintenance-log": "/system/maintenance-log",
  "operation-log": "/system/operation-log",
  "menu-manage": "/merchant-grant/menu-permission",
  menu: "/merchant-grant/menu-permission",
  角色管理: "/merchant-grant/role-permission",
  "role-manage": "/merchant-grant/role-permission",
  "user-manage": "/merchant-grant/backend-account",
  "admin-user": "/merchant-grant/backend-account",
  后台用户: "/merchant-grant/backend-account",
  "store-menu": "/merchant-grant/store-menu",
  "store-menu-permission": "/merchant-grant/store-menu",
  "department-manage": "/merchant-grant/department-manage"
};

const currentRouteTree = cloneDeep(
  businessRouteModules
) as unknown as CurrentRouteNode[];
const flatRouteRecords: FlatRouteRecord[] = [];
const routeByPath = new Map<string, FlatRouteRecord>();
const routeByTitle = new Map<string, FlatRouteRecord>();
const routeByName = new Map<string, FlatRouteRecord>();
const routeByToken = new Map<string, FlatRouteRecord>();

function normalizeToken(value: unknown) {
  return String(value ?? "")
    .trim()
    .toLowerCase()
    .replace(/\\/g, "/")
    .replace(/^\/+|\/+$/g, "");
}

function collectRouteRecords(
  routes: CurrentRouteNode[],
  parent?: CurrentRouteNode
) {
  routes.forEach(route => {
    const record: FlatRouteRecord = {
      path: route.path,
      name: String(route.name ?? ""),
      title: String(route.meta?.title ?? ""),
      parentPath: parent?.path ?? "",
      parentTitle: String(parent?.meta?.title ?? ""),
      visibleInSidebar: true
    };
    flatRouteRecords.push(record);

    const pathToken = normalizeToken(route.path);
    const nameToken = normalizeToken(route.name);
    const titleToken = normalizeToken(route.meta?.title);
    const lastPathToken = pathToken.split("/").filter(Boolean).pop();

    if (pathToken && !routeByPath.has(pathToken)) {
      routeByPath.set(pathToken, record);
    }
    if (nameToken && !routeByName.has(nameToken)) {
      routeByName.set(nameToken, record);
    }
    if (titleToken && !routeByTitle.has(titleToken)) {
      routeByTitle.set(titleToken, record);
    }
    if (lastPathToken && !routeByToken.has(lastPathToken)) {
      routeByToken.set(lastPathToken, record);
    }

    if (route.children?.length) {
      collectRouteRecords(route.children, route);
    }
  });
}

collectRouteRecords(currentRouteTree);

function findCurrentRouteRecord(menu: BackendMenuNode) {
  const aliasCandidates = [
    menu.frontRoute,
    menu.path,
    menu.name,
    menu.title
  ].map(normalizeToken);

  for (const token of aliasCandidates) {
    const aliasPath = backendMenuAliasMap[token];
    if (aliasPath) {
      const record = routeByPath.get(normalizeToken(aliasPath));
      if (record) return record;
    }
  }

  for (const token of aliasCandidates) {
    const exactRecord =
      routeByPath.get(token) ||
      routeByName.get(token) ||
      routeByTitle.get(token);
    if (exactRecord) return exactRecord;
  }

  for (const token of aliasCandidates) {
    const lastToken = token.split("/").filter(Boolean).pop();
    if (!lastToken) continue;
    const tokenRecord = routeByToken.get(lastToken);
    if (tokenRecord) return tokenRecord;
  }

  return null;
}

function collectAllowedPaths(
  menus: BackendMenuNode[],
  allowedPaths: Set<string>
) {
  menus.forEach(menu => {
    const currentRoute = findCurrentRouteRecord(menu);
    if (currentRoute?.path) {
      allowedPaths.add(currentRoute.path);
    }
    if (menu.children?.length) {
      collectAllowedPaths(menu.children, allowedPaths);
    }
  });
}

function filterCurrentRoutesByAllowedPaths(
  routes: CurrentRouteNode[],
  allowedPaths: Set<string>
) {
  return routes
    .map(route => {
      const children = route.children?.length
        ? filterCurrentRoutesByAllowedPaths(route.children, allowedPaths)
        : [];

      if (!allowedPaths.has(route.path) && children.length === 0) {
        return null;
      }

      const nextRoute = {
        ...route
      };
      if (children.length > 0) {
        nextRoute.children = children;
      } else {
        delete nextRoute.children;
      }

      return nextRoute;
    })
    .filter(Boolean) as CurrentRouteNode[];
}

export function buildPermissionRouteTreeFromBackend(
  menuTree: BackendMenuNode[] = []
) {
  const allowedPaths = new Set<string>();
  collectAllowedPaths(menuTree, allowedPaths);

  if (allowedPaths.size === 0) {
    return [];
  }

  // 工作台作为后台入口页保留，避免登录后因老菜单缺少首页节点而直接拦截。
  allowedPaths.add("/welcome");

  return filterCurrentRoutesByAllowedPaths(
    cloneDeep(currentRouteTree),
    allowedPaths
  );
}

export function resolveBackendMenuBinding(
  menu: BackendMenuNode
): BackendMenuBinding | null {
  const currentRoute = findCurrentRouteRecord(menu);
  if (!currentRoute) return null;

  return {
    currentPath: currentRoute.path,
    currentTitle: currentRoute.title,
    currentParentTitle: currentRoute.parentTitle,
    currentName: currentRoute.name,
    visibleInSidebar: currentRoute.visibleInSidebar
  };
}
