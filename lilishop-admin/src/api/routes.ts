import { http } from "@/utils/http";
import { isSuccessResult, unwrapResult } from "@/utils/result";
import { buildPermissionRouteTreeFromBackend } from "@/router/menu-sync";
import type { ResultMessage } from "./types";

type BackendMenuRecord = Record<string, any>;
type AsyncRouteRecord = Record<string, any>;

export const getAsyncRoutes = async (): Promise<
  ResultMessage<AsyncRouteRecord[]>
> => {
  const response = await http.request<ResultMessage<BackendMenuRecord[]>>(
    "get",
    "/manager/permission/menu/memberMenu"
  );

  if (!isSuccessResult(response)) {
    return response as ResultMessage<AsyncRouteRecord[]>;
  }

  const backendMenus = unwrapResult(response) ?? [];
  const routeTree = buildPermissionRouteTreeFromBackend(backendMenus);

  return {
    ...response,
    data: routeTree,
    result: routeTree
  };
};
