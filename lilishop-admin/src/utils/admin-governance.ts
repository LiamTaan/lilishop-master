import dayjs from "dayjs";
import { message } from "@/utils/message";

export type GovernanceRecord = Record<string, any>;

function normalizeStatusValue(status: unknown) {
  return String(status ?? "")
    .trim()
    .toUpperCase();
}

export function formatAdminDateTime(value: unknown) {
  if (value === undefined || value === null || value === "") {
    return "-";
  }
  const date = dayjs(value as any);
  if (!date.isValid()) {
    return String(value);
  }
  return date.format("YYYY-MM-DD HH:mm:ss");
}

function hasOwnArray(target: Record<string, any>, key: string) {
  return Array.isArray(target?.[key]);
}

export function getGoodsAuditStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["TOBEAUDITED", "PENDING", "WAIT", "NEW"].includes(value)) {
    return "待审核";
  }
  if (["PASS", "APPROVED", "SUCCESS"].includes(value)) {
    return "已通过";
  }
  if (["REFUSE", "REJECT", "REJECTED", "FAILED"].includes(value)) {
    return "已驳回";
  }
  return status || "-";
}

export function getGoodsAuditStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["TOBEAUDITED", "PENDING", "WAIT", "NEW"].includes(value)) {
    return "warning";
  }
  if (["PASS", "APPROVED", "SUCCESS"].includes(value)) {
    return "success";
  }
  if (["REFUSE", "REJECT", "REJECTED", "FAILED"].includes(value)) {
    return "danger";
  }
  return "info";
}

export function getGoodsMarketStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["UPPER", "UP", "ONLINE"].includes(value)) return "已上架";
  if (["DOWN", "OFFLINE"].includes(value)) return "已下架";
  return status || "-";
}

export function getGoodsMarketStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["UPPER", "UP", "ONLINE"].includes(value)) return "success";
  if (["DOWN", "OFFLINE"].includes(value)) return "info";
  return "info";
}

export function getGoodsTypeLabel(goodsType: unknown) {
  const value = normalizeStatusValue(goodsType);
  if (["PHYSICAL_GOODS"].includes(value)) return "实物商品";
  if (["VIRTUAL_GOODS"].includes(value)) return "虚拟商品";
  if (["E_COUPON"].includes(value)) return "电子卡券";
  return String(goodsType || "-");
}

export function getOrderStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["UNPAID", "WAIT_PAY"].includes(value)) return "待付款";
  if (["WAIT_PAY"].includes(value)) return "待付款";
  if (["PAID"].includes(value)) return "已付款";
  if (["WAIT_DELIVER", "UNDELIVERED"].includes(value)) return "待发货";
  if (["DELIVERED", "SHIPPED"].includes(value)) return "已发货";
  if (["WAIT_VERIFY", "TAKE"].includes(value)) return "待核销";
  if (["WAIT_TAKE", "PICKED_UP", "STAY_PICKED_UP"].includes(value)) {
    return "待提货";
  }
  if (["PENDING", "NEW", "CREATED"].includes(value)) return "待处理";
  if (["FINISHED", "SUCCESS", "DONE", "COMPLETE", "COMPLETED"].includes(value)) {
    return "已完成";
  }
  if (
    [
      "CANCEL",
      "CANCELLED",
      "CLOSED",
      "CLOSE",
      "REFUND",
      "REFUNDED",
      "FAILED"
    ].includes(value)
  ) {
    return "已关闭";
  }
  return status || "-";
}

export function getOrderStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["FINISHED", "SUCCESS", "DONE", "COMPLETE", "COMPLETED", "PAID"].includes(value)) {
    return "success";
  }
  if (
    [
      "UNPAID",
      "WAIT_PAY",
      "WAIT_DELIVER",
      "UNDELIVERED",
      "WAIT_VERIFY",
      "TAKE",
      "WAIT_TAKE",
      "STAY_PICKED_UP",
      "PENDING"
    ].includes(value)
  ) {
    return "warning";
  }
  if (
    [
      "CANCEL",
      "CANCELLED",
      "CLOSED",
      "CLOSE",
      "REFUND",
      "REFUNDED",
      "FAILED"
    ].includes(value)
  ) {
    return "danger";
  }
  return "info";
}

export function getOrderTypeLabel(orderType: unknown) {
  const value = normalizeStatusValue(orderType);
  if (["NORMAL", "COMMON"].includes(value)) return "普通订单";
  if (["PINTUAN", "GROUPON", "GROUP_BUY"].includes(value)) return "拼团订单";
  if (["WHOLESALE", "WHOLE_SALE"].includes(value)) return "批发订单";
  if (["POINTS", "POINT", "INTEGRAL"].includes(value)) return "积分订单";
  if (["GIFT"].includes(value)) return "赠品订单";
  if (["KANJIA", "BARGAIN"].includes(value)) return "砍价订单";
  if (["VIRTUAL"].includes(value)) return "虚拟订单";
  if (["E_COUPON", "COUPON"].includes(value)) return "电子卡券订单";
  return String(orderType || "-");
}

export function getFulfillmentTypeLabel(type: unknown) {
  const value = normalizeStatusValue(type);
  if (["LOGISTICS"].includes(value)) return "物流履约";
  if (["PICKUP"].includes(value)) return "自提核销";
  if (["VERIFICATION"].includes(value)) return "虚拟核验";
  return String(type || "-");
}

export function getPrimaryActionLabel(action: unknown) {
  const value = normalizeStatusValue(action);
  if (["RECEIVE"].includes(value)) return "确认收货";
  if (["VERIFY"].includes(value)) return "立即核销";
  return "无";
}

export function getAfterSaleStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["WAIT", "PENDING", "APPLY"].includes(value)) return "待处理";
  if (["PASS", "APPROVED", "AUDITED", "SUCCESS", "PROCESSING"].includes(value)) {
    return "处理中";
  }
  if (["REFUND", "DONE", "FINISHED", "COMPLETE", "COMPLETED"].includes(value)) {
    return "已完成";
  }
  if (["REJECTED", "CLOSED", "CANCELLED", "FAILED"].includes(value)) {
    return "已关闭";
  }
  return status || "-";
}

export function getAfterSaleStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["WAIT", "PENDING", "APPLY"].includes(value)) return "warning";
  if (["PASS", "APPROVED", "AUDITED", "SUCCESS", "PROCESSING"].includes(value)) {
    return "primary";
  }
  if (["REFUND", "DONE", "FINISHED", "COMPLETE", "COMPLETED"].includes(value)) {
    return "success";
  }
  if (["REJECTED", "CLOSED", "CANCELLED", "FAILED"].includes(value)) {
    return "danger";
  }
  return "info";
}

export function getVerifyStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["SUCCESS", "VERIFIED", "DONE", "COMPLETE", "COMPLETED"].includes(value)) {
    return "核销成功";
  }
  if (["FAIL", "FAILED"].includes(value)) return "核销失败";
  if (["PENDING", "WAIT", "WAIT_VERIFY"].includes(value)) return "待核销";
  if (["EXCEPTION", "ABNORMAL"].includes(value)) return "异常核销";
  if (["FAILED", "CLOSED", "CANCELLED"].includes(value)) return "核销失败";
  return status || "-";
}

export function getVerifyStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["SUCCESS", "VERIFIED", "DONE", "COMPLETE", "COMPLETED"].includes(value)) {
    return "success";
  }
  if (["FAIL", "FAILED"].includes(value)) return "danger";
  if (["PENDING", "WAIT", "WAIT_VERIFY"].includes(value)) return "warning";
  if (["EXCEPTION", "ABNORMAL"].includes(value)) return "danger";
  if (["FAILED", "CLOSED", "CANCELLED"].includes(value)) return "danger";
  return "info";
}

export function getVerificationSourceTypeLabel(type: unknown) {
  const value = normalizeStatusValue(type);
  if (["RESOURCE", "SOURCE"].includes(value)) return "验证码源";
  if (["SLIDER"].includes(value)) return "滑块";
  return String(type || "-");
}

export function normalizeVerificationSourceType(type: unknown) {
  const value = normalizeStatusValue(type);
  if (["RESOURCE", "SOURCE"].includes(value)) return "RESOURCE";
  if (["SLIDER"].includes(value)) return "SLIDER";
  return String(type || "");
}

export function getStoreAuditStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["NONE", "N/A", "NA"].includes(value)) return "未发起";
  if (["DRAFT"].includes(value)) return "草稿";
  if (["SUBMITTED", "PENDING", "WAIT"].includes(value)) return "待审核";
  if (["APPROVED", "PASS", "SUCCESS"].includes(value)) return "审核通过";
  if (["REJECTED", "REFUSE", "FAILED"].includes(value)) return "审核驳回";
  if (["FROZEN"].includes(value)) return "已冻结";
  return String(status || "-");
}

export function getStoreAuditStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["APPROVED", "PASS", "SUCCESS"].includes(value)) return "success";
  if (["SUBMITTED", "PENDING", "WAIT", "DRAFT"].includes(value)) return "warning";
  if (["REJECTED", "REFUSE", "FAILED", "FROZEN"].includes(value)) return "danger";
  if (["NONE", "N/A", "NA"].includes(value)) return "info";
  return "info";
}

export function getStoreStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["OPEN", "ENABLE", "APPROVED", "TRUE", "1"].includes(value)) return "营业中";
  if (["APPLY", "APPLYING"].includes(value)) return "申请中";
  if (["REFUSED"].includes(value)) return "审核拒绝";
  if (["CLOSE", "CLOSED", "DISABLE", "FALSE", "0"].includes(value)) return "已停业";
  if (["FROZEN"].includes(value)) return "已冻结";
  return String(status || "-");
}

export function getStoreStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["OPEN", "ENABLE", "APPROVED", "TRUE", "1"].includes(value)) return "success";
  if (["APPLY", "APPLYING"].includes(value)) return "warning";
  if (["REFUSED", "FROZEN"].includes(value)) return "danger";
  if (["CLOSE", "CLOSED", "DISABLE", "FALSE", "0"].includes(value)) return "info";
  return "info";
}

export function getBindStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["BOUND", "APPROVED", "SUCCESS"].includes(value)) return "已绑定";
  if (["PENDING", "WAIT"].includes(value)) return "待审核";
  if (["REJECTED"].includes(value)) return "已驳回";
  if (["UNBOUND"].includes(value)) return "未绑定";
  if (["DISABLED", "CLOSE"].includes(value)) return "已停用";
  return String(status || "-");
}

export function getBindStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["BOUND", "APPROVED", "SUCCESS"].includes(value)) return "success";
  if (["PENDING", "WAIT"].includes(value)) return "warning";
  if (["REJECTED", "UNBOUND", "DISABLED", "CLOSE"].includes(value)) return "danger";
  return "info";
}

export function getStoreBizTypeLabel(bizType: unknown) {
  const value = normalizeStatusValue(bizType);
  if (["SUPPLIER"].includes(value)) return "供货商";
  if (["AGENT"].includes(value)) return "代理商";
  return String(bizType || "-");
}

export function getAgentLevelLabel(agentLevel: unknown) {
  const value = normalizeStatusValue(agentLevel);
  if (["CITY"].includes(value)) return "市级代理";
  if (["COUNTY"].includes(value)) return "区县代理";
  if (["TOWNSHIP"].includes(value)) return "乡镇代理";
  if (["WHOLESALER"].includes(value)) return "批发商代理";
  return String(agentLevel || "-");
}

export function getAgentLevelType(agentLevel: unknown) {
  const value = normalizeStatusValue(agentLevel);
  if (["CITY"].includes(value)) return "danger";
  if (["COUNTY"].includes(value)) return "warning";
  if (["TOWNSHIP"].includes(value)) return "success";
  if (["WHOLESALER"].includes(value)) return "info";
  return "info";
}

export function getBillStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["OUT"].includes(value)) return "已出账";
  if (["CHECK", "RECON"].includes(value)) return "已核对";
  if (["EXAMINE", "PASS", "AUDITED"].includes(value)) return "已审核";
  if (["PAY"].includes(value)) return "已付款";
  if (["COMPLETE", "COMPLETED"].includes(value)) return "已完成";
  return String(status || "-");
}

export function getBillStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["OUT", "CHECK", "RECON"].includes(value)) return "warning";
  if (["EXAMINE", "PASS", "AUDITED"].includes(value)) return "primary";
  if (["PAY", "COMPLETE", "COMPLETED"].includes(value)) return "success";
  return "info";
}

export function getProfitSharingSettlementStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["WAIT_COMPLETE"].includes(value)) return "待订单完成";
  if (["PROCESSING", "WAIT", "PENDING"].includes(value)) return "处理中";
  if (["FINISHED", "SUCCESS", "DONE", "COMPLETED"].includes(value)) {
    return "分账完成";
  }
  if (["ORDER_CANCEL", "CANCEL", "CANCELLED"].includes(value)) {
    return "订单取消";
  }
  return String(status || "-");
}

export function getProfitSharingSettlementStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["WAIT_COMPLETE", "PROCESSING", "WAIT", "PENDING"].includes(value)) {
    return "warning";
  }
  if (["FINISHED", "SUCCESS", "DONE", "COMPLETED"].includes(value)) {
    return "success";
  }
  if (["ORDER_CANCEL", "CANCEL", "CANCELLED"].includes(value)) {
    return "danger";
  }
  return "info";
}

export function getProfitSharingRoleLabel(roleType: unknown) {
  const value = normalizeStatusValue(roleType);
  if (["AGENT"].includes(value)) return "代理商";
  if (["STORE", "SUPPLIER"].includes(value)) return "供货商";
  if (["PLATFORM"].includes(value)) return "平台";
  if (["MEMBER", "BUYER"].includes(value)) return "会员";
  return String(roleType || "-");
}

export function getPromotionStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["NEW"].includes(value)) return "待开始";
  if (["START", "STARTED", "RUNNING", "OPEN", "ONLINE"].includes(value)) {
    return "进行中";
  }
  if (["END", "ENDED", "OFFLINE"].includes(value)) return "已结束";
  if (["CLOSE", "CLOSED"].includes(value)) return "已关闭";
  return String(status || "-");
}

export function getPromotionStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["NEW"].includes(value)) return "warning";
  if (["START", "STARTED", "RUNNING", "OPEN", "ONLINE"].includes(value)) {
    return "success";
  }
  if (["END", "ENDED", "OFFLINE"].includes(value)) return "info";
  if (["CLOSE", "CLOSED"].includes(value)) return "danger";
  return "info";
}

export function getWithdrawApplyStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["APPLY"].includes(value)) return "申请中";
  if (["VIA_AUDITING", "D_VIA_AUDITING"].includes(value)) return "审核通过";
  if (["FAIL_AUDITING", "D_FAIL_AUDITING"].includes(value)) return "审核未通过";
  if (["SUCCESS"].includes(value)) return "提现成功";
  if (["ERROR"].includes(value)) return "提现失败";
  return String(status || "-");
}

export function getWithdrawApplyStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["APPLY"].includes(value)) return "warning";
  if (["VIA_AUDITING", "D_VIA_AUDITING", "SUCCESS"].includes(value)) {
    return "success";
  }
  if (["FAIL_AUDITING", "D_FAIL_AUDITING", "ERROR"].includes(value)) {
    return "danger";
  }
  return "info";
}

export function getProcurementStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["DRAFT"].includes(value)) return "待提交";
  if (["SUBMITTED"].includes(value)) return "已提交";
  if (["PENDING_INBOUND"].includes(value)) return "待入库";
  if (["PARTIAL_INBOUND"].includes(value)) return "部分入库";
  if (["COMPLETED"].includes(value)) return "已完成";
  if (["CLOSED"].includes(value)) return "已关闭";
  if (["REJECTED"].includes(value)) return "已拒绝";
  return String(status || "-");
}

export function getProcurementStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["DRAFT", "SUBMITTED", "PENDING_INBOUND", "PARTIAL_INBOUND"].includes(value)) {
    return "warning";
  }
  if (["COMPLETED"].includes(value)) return "success";
  if (["CLOSED", "REJECTED"].includes(value)) return "danger";
  return "info";
}

export function getEnableStatusLabel(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["ENABLE", "ENABLED", "OPEN", "TRUE", "1"].includes(value)) return "启用";
  if (["DISABLE", "DISABLED", "CLOSE", "CLOSED", "FALSE", "0"].includes(value)) {
    return "停用";
  }
  return String(status || "-");
}

export function getEnableStatusType(status: unknown) {
  const value = normalizeStatusValue(status);
  if (["ENABLE", "ENABLED", "OPEN", "TRUE", "1"].includes(value)) return "success";
  if (["DISABLE", "DISABLED", "CLOSE", "CLOSED", "FALSE", "0"].includes(value)) {
    return "info";
  }
  return "info";
}

export function getWalletServiceTypeLabel(serviceType: unknown) {
  const value = normalizeStatusValue(serviceType);
  if (["WALLET_RECHARGE", "RECHARGE", "TOPUP", "INCOME"].includes(value)) {
    return "余额充值";
  }
  if (["WALLET_PAY", "PAY", "EXPENSE"].includes(value)) return "余额支付";
  if (["WALLET_REFUND", "REFUND"].includes(value)) return "余额退款";
  if (["WALLET_WITHDRAWAL", "WITHDRAW", "PAYOUT"].includes(value)) {
    return "余额提现";
  }
  if (["WALLET_COMMISSION", "COMMISSION"].includes(value)) return "佣金提成";
  if (["FREEZE"].includes(value)) return "冻结";
  if (["UNFREEZE"].includes(value)) return "解冻";
  return String(serviceType || "-");
}

export function getWalletServiceTypeTagType(serviceType: unknown) {
  const value = normalizeStatusValue(serviceType);
  if (["WALLET_RECHARGE", "RECHARGE", "TOPUP", "INCOME", "WALLET_REFUND", "REFUND"].includes(value)) {
    return "success";
  }
  if (["WALLET_PAY", "PAY", "EXPENSE", "WALLET_WITHDRAWAL", "WITHDRAW", "PAYOUT"].includes(value)) {
    return "warning";
  }
  if (["WALLET_COMMISSION", "COMMISSION"].includes(value)) return "primary";
  if (["FREEZE", "UNFREEZE"].includes(value)) return "info";
  return "info";
}

export function extractApiPayload<T = any>(response: any): T {
  return (response?.result ?? response?.data ?? response) as T;
}

export function extractApiRecords<T extends GovernanceRecord = GovernanceRecord>(
  response: any
): T[] {
  const queue: any[] = [response, extractApiPayload<any>(response)];
  const visited = new Set<any>();

  while (queue.length > 0) {
    const current = queue.shift();
    if (!current || visited.has(current)) continue;
    visited.add(current);

    if (Array.isArray(current)) return current as T[];
    if (typeof current !== "object") continue;

    if (hasOwnArray(current, "records")) return current.records as T[];
    if (hasOwnArray(current, "list")) return current.list as T[];
    if (hasOwnArray(current, "rows")) return current.rows as T[];
    if (hasOwnArray(current, "items")) return current.items as T[];
    if (hasOwnArray(current, "content")) return current.content as T[];

    ["result", "data", "page", "payload"].forEach(key => {
      if (current[key]) queue.push(current[key]);
    });
  }

  return [];
}

export function createPendingActionHandler(moduleName: string) {
  return (action: string) => {
    message(`${moduleName}${action}功能待后端联调后接入`, { type: "info" });
  };
}
