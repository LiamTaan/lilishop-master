import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

function joinIds(ids: string[]) {
  return ids
    .map(item => String(item || "").trim())
    .filter(Boolean)
    .join(",");
}

export const getWholesaleGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/goods/wholesale/list",
    { params }
  );
};

export const getGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>("get", "/manager/goods/goods/list", {
    params
  });
};

export const getWholesaleGoodsDetail = (goodsId: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/goods/get/${goodsId}`
  );
};

export const createWholesaleGoods = (data: Record<string, any>) => {
  return http.request<ResultMessage<null>>("post", "/manager/goods/goods", {
    data
  });
};

export const updateWholesaleGoods = (
  goodsId: string,
  data: Record<string, any>
) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/goods/goods/${goodsId}`,
    { data }
  );
};

export const getWholesaleSkuPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/goods/sku/list",
    { params }
  );
};

export const auditWholesaleGoods = (
  goodsIds: string[],
  authFlag: "PASS" | "REFUSE"
) => {
  return http.request<ResultMessage<null>>("put", "/manager/goods/goods/auth", {
    data: { goodsIds: joinIds(goodsIds), authFlag }
  });
};

export const upperWholesaleGoods = (goodsIds: string[]) => {
  return http.request<ResultMessage<null>>("put", "/manager/goods/goods/up", {
    data: { goodsId: joinIds(goodsIds) }
  });
};

export const underWholesaleGoods = (goodsIds: string[], reason: string) => {
  return http.request<ResultMessage<null>>(
    "put",
    "/manager/goods/goods/under",
    {
      data: { goodsId: joinIds(goodsIds), reason }
    }
  );
};

export const deleteWholesaleGoods = (goodsIds: string[]) => {
  return http.request<ResultMessage<null>>(
    "delete",
    `/manager/goods/goods/${goodsIds.join(",")}`
  );
};

export const getCategoryPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/goods/category/allChildren",
    { params }
  );
};

export const getStoreOptions = () => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/store/store/all"
  );
};

export const getStoreFreightTemplates = (storeId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/goods/freightTemplate/store/${storeId}`
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

export const getPointsGoodsDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/promotion/pointsGoods/${id}`
  );
};

export const deletePointsGoods = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/pointsGoods/${ids.join(",")}`
  );
};

export const getCardCouponGoodsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "get",
    "/manager/promotion/cardCouponGoods",
    { params }
  );
};

export const deleteCardCouponGoods = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/promotion/cardCouponGoods/${ids.join(",")}`
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
    data: params
  });
};

export const updateBrand = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", `/manager/goods/brand/${id}`, {
    data: params
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
  return http.request<ResultMessage<any>>("get", "/manager/goods/parameters", {
    params
  });
};

export const getCategoryParameterOptions = (categoryId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/goods/categoryParameters/${categoryId}`
  );
};

export const getParameterDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/parameters/${id}`
  );
};

export const createParameter = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/goods/parameters", {
    data: params
  });
};

export const updateParameter = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("put", "/manager/goods/parameters", {
    data: params
  });
};

export const deleteParameter = (id: string) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/goods/parameters/${id}`
  );
};

export const getGoodsUnitPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>("get", "/manager/goods/goodsUnit", {
    params
  });
};

export const getGoodsUnitDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/goods/goodsUnit/get/${id}`
  );
};

export const createGoodsUnit = (params: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/goods/goodsUnit", {
    data: params
  });
};

export const updateGoodsUnit = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/goods/goodsUnit/${id}`,
    { data: params }
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
  return http.request<ResultMessage<any>>("post", "/manager/goods/goodsGroup", {
    data: params
  });
};

export const updateGoodsGroup = (id: string, params: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/goods/goodsGroup/update/${id}`,
    { data: params }
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
    { data: goodsIds }
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
