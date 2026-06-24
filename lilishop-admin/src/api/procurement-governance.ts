import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getProcurementOrderPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>("get", "/manager/procurement/order", {
    params
  });
};

export const getProcurementOrderDetail = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/order/${id}`
  );
};

export const getProcurementInboundPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/procurement/inbound",
    { params }
  );
};

export const getProcurementInboundDetail = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/inbound/${id}`
  );
};

export const getProcurementInboundItems = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/inbound/${id}/items`
  );
};

export const getInventoryCountPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/procurement/inventory-count/page",
    { params }
  );
};

export const getInventoryCountDetail = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/inventory-count/${id}`
  );
};

export const getInventoryCountItemsPage = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/inventory-count/${id}/items/page`,
    { params }
  );
};

export const downloadInventoryCountItems = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/inventory-count/${id}/download`
  );
};

export const getDamageReportPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/procurement/damage-report/page",
    { params }
  );
};

export const getDamageReportDetail = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/damage-report/${id}`
  );
};

export const getDamageReportItems = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/procurement/damage-report/${id}/items`
  );
};

export const getStockReasonPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/procurement/reason",
    {
      params
    }
  );
};

export const createStockReason = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/procurement/reason",
    {
      data
    }
  );
};

export const updateStockReason = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/procurement/reason",
    {
      data
    }
  );
};

export const deleteStockReason = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/procurement/reason/${id}`
  );
};
