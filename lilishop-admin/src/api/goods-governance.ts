import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getWholesaleGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/goods/wholesale/list",
    { params }
  );
};

export const getWholesaleGoodsDetail = (goodsId: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/goods/get/${goodsId}`
  );
};

export const auditWholesaleGoods = (
  goodsIds: string[],
  authFlag: "PASS" | "REFUSE"
) => {
  return http.request<ResultMessage<null>>("put", "/manager/goods/goods/auth", {
    params: { goodsIds, authFlag }
  });
};

export const upperWholesaleGoods = (goodsIds: string[]) => {
  return http.request<ResultMessage<null>>("put", "/manager/goods/goods/up", {
    params: { goodsId: goodsIds }
  });
};

export const underWholesaleGoods = (goodsIds: string[], reason: string) => {
  return http.request<ResultMessage<null>>(
    "put",
    "/manager/goods/goods/under",
    {
      params: { goodsId: goodsIds, reason }
    }
  );
};

export const getCategoryPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/category/allChildren",
    { params }
  );
};

export const createCategory = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/goods/category", {
    params
  });
};

export const updateCategory = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", "/manager/goods/category", {
    params
  });
};

export const deleteCategory = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/category/${id}`
  );
};

export const getPointsGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/pointsGoods",
    { params }
  );
};

export const getCardCouponGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/cardCouponGoods",
    { params }
  );
};

export const getBrandPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/brand/getByPage",
    { params }
  );
};

export const getBrandDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/brand/get/${id}`
  );
};

export const getBrandOptions = () => {
  return http.request<ResultMessage<any[]>>("get", "/manager/goods/brand/all");
};

export const createBrand = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/goods/brand", {
    params
  });
};

export const updateBrand = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", `/manager/goods/brand/${id}`, {
    params
  });
};

export const updateBrandDisable = (brandId: string, disable: boolean) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/goods/brand/disable/${brandId}`,
    { params: { disable } }
  );
};

export const deleteBrands = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/brand/delByIds/${ids.join(",")}`
  );
};

export const getParameterPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/parameters",
    { params }
  );
};

export const getParameterDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/parameters/${id}`
  );
};

export const createParameter = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/goods/parameters",
    { params }
  );
};

export const updateParameter = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", "/manager/goods/parameters", {
    params
  });
};

export const deleteParameter = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/parameters/${id}`
  );
};

export const getGoodsUnitPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/goodsUnit",
    { params }
  );
};

export const getGoodsUnitDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/goodsUnit/get/${id}`
  );
};

export const createGoodsUnit = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/goods/goodsUnit",
    { params }
  );
};

export const updateGoodsUnit = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/goods/goodsUnit/${id}`,
    { params }
  );
};

export const deleteGoodsUnits = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/goodsUnit/delete/${ids.join(",")}`
  );
};

export const getCategoryBrandCategories = (brandId: string) => {
  return http.request<ResultMessage<any[]>>(
    "get",
    `/manager/goods/categoryBrand/${brandId}`
  );
};

export const saveCategoryBrandCategories = (
  brandId: string,
  categoryIds: string[]
) => {
  return http.request<ResultMessage<any>>(
    "post",
    `/manager/goods/categoryBrand/${brandId}`,
    { data: categoryIds }
  );
};

export const getGoodsGroupPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/goodsGroup/getByPage",
    { params }
  );
};

export const getGoodsGroupDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/goodsGroup/get/${id}`
  );
};

export const createGoodsGroup = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/goods/goodsGroup",
    { params }
  );
};

export const updateGoodsGroup = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/goods/goodsGroup/update/${id}`,
    { params }
  );
};

export const deleteGoodsGroup = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/goodsGroup/delete/${id}`
  );
};

export const updateGoodsGroupGoods = (groupId: string, goodsIds: string[]) => {
  return http.request<ResultMessage<any>>(
    "post",
    `/manager/goods/goodsGroup/${groupId}/goods`,
    { params: { goodsIds: goodsIds.join(",") } }
  );
};

export const getGoodsGroupGoodsPage = (
  groupId: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/goods/goodsGroup/${groupId}/goods/getByPage`,
    { params }
  );
};
