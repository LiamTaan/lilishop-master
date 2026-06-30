import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getHomeAdvertisementConfig = (clientType: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/homeAdvertisement",
    { params: { clientType } }
  );
};

export const saveHomeAdvertisementConfig = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/other/homeAdvertisement",
    { data }
  );
};

export const getHomeRecommendationStrategy = (clientType: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/homeRecommendationStrategy",
    { params: { clientType } }
  );
};

export const saveHomeRecommendationStrategy = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/other/homeRecommendationStrategy",
    { data }
  );
};
