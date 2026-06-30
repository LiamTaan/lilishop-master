import dayjs from "dayjs";
import { formatAdminDateTime, getPromotionStatusLabel } from "@/utils/admin-governance";

type AnyRecord = Record<string, any>;

export const promotionScopeOptions = [
  { label: "指定商品", value: "PORTION_GOODS" },
  { label: "全部商品", value: "ALL" },
  { label: "指定分类", value: "PORTION_GOODS_CATEGORY" },
  { label: "店铺分类", value: "PORTION_SHOP_CATEGORY" }
];

export function createPromotionGoodsItem(source: AnyRecord = {}): AnyRecord {
  return {
    skuId: source.skuId || "",
    goodsId: source.goodsId || "",
    goodsName: source.goodsName || "",
    thumbnail: source.thumbnail || "",
    price: toNumber(source.price),
    originalPrice: toNumber(source.originalPrice),
    quantity: toNumber(source.quantity),
    limitNum: toNumber(source.limitNum),
    points: toNumber(source.points),
    scopeId: source.scopeId || source.skuId || "",
    scopeType: source.scopeType || "PORTION_GOODS"
  };
}

export function createCouponActivityItem(source: AnyRecord = {}): AnyRecord {
  return {
    couponId: source.couponId || "",
    couponName: source.couponName || "",
    couponType: source.couponType || "PRICE",
    price: toNumber(source.price),
    couponDiscount: toNumber(source.couponDiscount),
    num: toNumber(source.num) || 1
  };
}

export function createMemberItem(source: AnyRecord = {}): AnyRecord {
  return {
    id: source.id || "",
    nickName: source.nickName || source.memberName || ""
  };
}

export function createKanjiaGoodsItem(source: AnyRecord = {}): AnyRecord {
  return {
    id: source.id || "",
    promotionName: source.promotionName || "",
    skuId: source.skuId || "",
    goodsId: source.goodsId || "",
    goodsName: source.goodsName || "",
    thumbnail: source.thumbnail || "",
    settlementPrice: toNumber(source.settlementPrice),
    purchasePrice: toNumber(source.purchasePrice),
    lowestPrice: toNumber(source.lowestPrice),
    highestPrice: toNumber(source.highestPrice),
    stock: toNumber(source.stock),
    originalPrice: toNumber(source.originalPrice)
  };
}

export function formatDateTimeForField(value: unknown) {
  if (value === undefined || value === null || value === "") {
    return "";
  }
  const date = dayjs(value as any);
  if (!date.isValid()) {
    return String(value);
  }
  return date.format("YYYY-MM-DD HH:mm:ss");
}

export function toTimeValue(value: string) {
  return value ? new Date(value).getTime() : undefined;
}

export function normalizePromotionDetail<T extends AnyRecord>(item: T) {
  return {
    ...item,
    id: item.id || item.promotionId || item.couponId || item.couponActivityId || item.seckillId,
    promotionStatusLabel:
      item.promotionStatusLabel || getPromotionStatusLabel(item.promotionStatus || item.status || "-"),
    startTimeText: formatAdminDateTime(item.startTime),
    endTimeText: formatAdminDateTime(item.endTime),
    createTimeText: formatAdminDateTime(item.createTime),
    updateTimeText: formatAdminDateTime(item.updateTime)
  };
}

export function toNumber(value: unknown) {
  const num = Number(value);
  return Number.isFinite(num) ? num : 0;
}

export function formatMoney(value: unknown) {
  return `¥ ${toNumber(value).toFixed(2)}`;
}

export function formatDiscount(value: unknown) {
  const num = toNumber(value);
  return num ? `${num} 折` : "-";
}

export function formatBooleanLabel(value: unknown) {
  return value ? "是" : "否";
}

export function formatArrayText(
  list: unknown[],
  formatter: (item: any, index: number) => string
) {
  if (!Array.isArray(list) || list.length === 0) {
    return "-";
  }
  return list.map(formatter).join("\n");
}

export function extractMemberListFromScopeInfo(scopeInfo: unknown) {
  if (Array.isArray(scopeInfo)) {
    return scopeInfo.map(item => createMemberItem(item));
  }
  if (typeof scopeInfo !== "string" || !scopeInfo.trim()) {
    return [];
  }
  try {
    const parsed = JSON.parse(scopeInfo);
    return Array.isArray(parsed) ? parsed.map(item => createMemberItem(item)) : [];
  } catch (_error) {
    return [];
  }
}
