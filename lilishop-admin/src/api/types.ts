export interface ResultMessage<T> {
  success?: boolean;
  message?: string;
  code?: number;
  timestamp?: number;
  result?: T;
  data?: T;
}

export interface PageResult<T> {
  records?: T[];
  total?: number;
  size?: number;
  current?: number;
}

export interface WholesaleDashboardResult {
  indexStatistics?: Record<string, any>;
  storeRankList?: Record<string, any>[];
  goodsRankList?: Record<string, any>[];
  categoryRankList?: Record<string, any>[];
  profitSharingBalance?: Record<string, any>;
  agentFundSummary?: Record<string, any>;
  agentOverview?: Record<string, any>;
}

export interface AgentRoleRelationVO {
  id: string;
  memberId: string;
  memberName: string;
  mobile: string;
  roleCode: string;
  regionId: string;
  regionName: string;
  agentLevel: string;
  status: string;
  remark: string;
}

export interface AgentStoreBindVO {
  id: string;
  agentMemberId: string;
  agentMemberName: string;
  storeId: string;
  storeName: string;
  regionId: string;
  regionName: string;
  bindStatus: string;
  auditStatus: string;
  auditRemark: string;
  remark: string;
}

export interface StoreAuditDTO {
  auditStatus: "APPROVED" | "REJECTED" | "FROZEN";
  auditRemark?: string;
}

export interface AgentStoreBindAuditDTO {
  auditStatus: "APPROVED" | "REJECTED";
  auditRemark?: string;
}
