import { http } from "@/utils/http";
import type { ResultMessage, WholesaleDashboardResult } from "./types";

type DashboardMapResult = ResultMessage<Record<string, any>>;

export const getWholesaleDashboard = () => {
  return http.request<ResultMessage<WholesaleDashboardResult>>(
    "get",
    "/manager/dashboard/wholesale"
  );
};

export const getIndexNotice = () => {
  return http.request<DashboardMapResult>(
    "get",
    "/manager/statistics/index/notice"
  );
};

export const getPlatformOverview = () => {
  return http.request<DashboardMapResult>("get", "/manager/statistics/index");
};

export const getStoreRank = () => {
  return http.request<DashboardMapResult>(
    "get",
    "/manager/statistics/index/storeStatistics"
  );
};

export const getGoodsRank = () => {
  return http.request<DashboardMapResult>(
    "get",
    "/manager/statistics/index/goodsStatistics"
  );
};

export const getOrderOverview = () => {
  return http.request<DashboardMapResult>(
    "get",
    "/manager/statistics/order/overview"
  );
};
