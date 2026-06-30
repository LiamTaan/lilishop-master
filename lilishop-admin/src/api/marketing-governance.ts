import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getPintuanPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/pintuan",
    { params }
  );
};

export const getPintuanDetail = (id: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/promotion/pintuan/${id}`
  );
};

export const getPintuanGoodsPage = (
  pintuanId: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/promotion/pintuan/goods/${pintuanId}`,
    { params }
  );
};

export const getPintuanMembers = (pintuanId: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/promotion/pintuan/${pintuanId}/members`
  );
};

export const createPintuan = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/promotion/pintuan", {
    data
  });
};

export const updatePintuan = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/promotion/pintuan/${id}`,
    { data }
  );
};

export const deletePintuan = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/pintuan/${ids.join(",")}`
  );
};

export const updatePintuanStatus = (
  pintuanIds: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/promotion/pintuan/status/${pintuanIds}`,
    { params }
  );
};

export const getCouponPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/coupon",
    { params }
  );
};

export const getCouponDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/coupon/${id}`
  );
};

export const createCoupon = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/promotion/coupon", {
    data
  });
};

export const updateCoupon = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", "/manager/promotion/coupon", {
    data
  });
};

export const updateCouponStatus = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/promotion/coupon/status",
    { params }
  );
};

export const deleteCoupons = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/coupon/${ids.join(",")}`
  );
};

export const getCouponMembers = (id: string, params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/promotion/coupon/member/${id}`,
    { params }
  );
};

export const getReceivedCoupons = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/coupon/received",
    { params }
  );
};

export const getCouponActivityPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/couponActivity",
    { params }
  );
};

export const getCouponActivityDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/couponActivity/${id}`
  );
};

export const createCouponActivity = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/promotion/couponActivity",
    { data }
  );
};

export const deleteCouponActivity = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/couponActivity/${id}`
  );
};

export const getFullDiscountPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/fullDiscount",
    { params }
  );
};

export const getFullDiscountDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/fullDiscount/${id}`
  );
};

export const updateFullDiscountStatus = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/promotion/fullDiscount/status/${id}`,
    { params }
  );
};

export const getSeckillInit = () => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/seckill/init"
  );
};

export const getSeckillPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/seckill",
    { params }
  );
};

export const getSeckillDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/seckill/${id}`
  );
};

export const updateSeckill = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/promotion/seckill",
    { data }
  );
};

export const deleteSeckill = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/seckill/${id}`
  );
};

export const updateSeckillStatus = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/promotion/seckill/status/${id}`,
    { params }
  );
};

export const getSeckillApplyPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/seckill/apply",
    { params }
  );
};

export const deleteSeckillApply = (seckillId: string, id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/seckill/apply/${seckillId}/${id}`
  );
};

export const getKanjiaPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/kanJiaGoods",
    { params }
  );
};

export const getKanjiaDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/kanJiaGoods/${id}`
  );
};

export const createKanjia = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/promotion/kanJiaGoods",
    { data }
  );
};

export const updateKanjia = (data: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/promotion/kanJiaGoods",
    { data }
  );
};

export const deleteKanjia = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/kanJiaGoods/${ids.join(",")}`
  );
};
