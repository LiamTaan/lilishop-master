import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getProfitSharingRulePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/profitsharing/rule",
    { params }
  );
};

export const createProfitSharingRule = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/profitsharing/rule",
    { data }
  );
};

export const updateProfitSharingRule = (
  id: string,
  data: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/profitsharing/rule/${id}`,
    { data }
  );
};

export const deleteProfitSharingRule = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/profitsharing/rule/${id}`
  );
};

export const getProfitSharingRecordPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/profitsharing/record",
    { params }
  );
};

export const getProfitSharingBalance = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/profitsharing/balance",
    { params }
  );
};

export const getWalletAccountPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/wallet/wallet/page",
    { params }
  );
};

export const getMemberWallet = (memberId: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/wallet/wallet",
    { params: { memberId } }
  );
};

export const getWithdrawApplyPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/wallet/withdrawApply",
    { params }
  );
};

export const getWalletLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/wallet/log",
    { params }
  );
};

export const getProcurementReconciliationPage = (
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/reconciliation/purchase",
    { params }
  );
};

export const getFundReconciliationPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/reconciliation/fund",
    { params }
  );
};

export const getBillPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/order/bill/getByPage",
    { params }
  );
};

export const getBillDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/order/bill/get/${id}`
  );
};

export const getBillStoreFlowPage = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/order/bill/${id}/getStoreFlow`,
    { params }
  );
};

export const payBill = (id: string) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/order/bill/pay/${id}`
  );
};
