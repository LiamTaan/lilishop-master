import { http } from "@/utils/http";
import type {
  AgentStoreBindAuditDTO,
  AgentRoleRelationVO,
  AgentStoreBindVO,
  PageResult,
  ResultMessage,
  StoreAuditDTO
} from "./types";

export const getStoreApplyPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<PageResult<Record<string, any>>>>(
    "get",
    "/manager/store/store",
    { params }
  );
};

export const getStoreDetail = (storeId: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/store/store/get/detail/${storeId}`
  );
};

export const getStoreAuditLog = (storeId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/store/store/audit/log/${storeId}`
  );
};

export const getAgentRolePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<AgentRoleRelationVO[]>>(
    "get",
    "/manager/agent/role",
    { params }
  );
};

export const getAgentStorePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<AgentStoreBindVO[]>>(
    "get",
    "/manager/agent/store",
    { params }
  );
};

export const auditStore = (storeId: string, data: StoreAuditDTO) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/store/store/audit/${storeId}`,
    { data }
  );
};

export const disableStore = (storeId: string) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/store/store/disable/${storeId}`
  );
};

export const enableStore = (storeId: string) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/store/store/enable/${storeId}`
  );
};

export const auditAgentBind = (
  bindId: string,
  data: AgentStoreBindAuditDTO
) => {
  return http.request<ResultMessage<AgentStoreBindVO>>(
    "put",
    `/manager/agent/store/${bindId}/audit`,
    { data }
  );
};

export const unbindAgentBind = (bindId: string) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/agent/store/${bindId}/unbind`
  );
};
