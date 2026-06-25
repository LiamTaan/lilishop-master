import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export const getPlatformHomeConfig = (clientType: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/platformHomeConfig",
    { params: { clientType } }
  );
};

export const savePlatformHomeConfig = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/other/platformHomeConfig",
    { data }
  );
};
