import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getOrderPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/order",
    { params }
  );
};

export const getOrderDetail = (orderSn: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/order/order/${orderSn}`
  );
};

export const getAfterSalePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/afterSale/page",
    { params }
  );
};

export const getVerificationRecordPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/other/verificationRecord",
    { params }
  );
};

export const getVerificationRuleSetting = () => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/setting/setting/get/VERIFICATION_RULE_SETTING"
  );
};

export const saveVerificationRuleSetting = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/setting/setting/put/VERIFICATION_RULE_SETTING",
    { data: JSON.stringify(data) }
  );
};

export const getOrderComplaintPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/complain",
    { params }
  );
};

export const getOrderComplaintDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/order/complain/${id}`
  );
};

export const updateOrderComplaint = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", "/manager/order/complain", {
    params
  });
};

export const createOrderComplaintCommunication = (
  complainId: string,
  content: string
) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/order/complain/communication",
    { params: { complainId, content } }
  );
};

export const updateOrderComplaintStatus = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/order/complain/status",
    { params }
  );
};

export const completeOrderComplaint = (
  id: string,
  arbitrationResult: string
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/order/complain/complete/${id}`,
    { params: { arbitrationResult } }
  );
};

export const getRefundLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/refundLog",
    { params }
  );
};

export const getRefundLogDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/order/refundLog/${id}`
  );
};

export const getPaymentLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/paymentLog",
    { params }
  );
};

export const getReceiptPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/trade/receipt",
    { params }
  );
};

export const getOrderLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/orderLog/getByPage",
    { params }
  );
};

export const getOrderLogDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/order/orderLog/get/${id}`
  );
};
