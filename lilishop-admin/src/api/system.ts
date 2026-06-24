import { http } from "@/utils/http";
import { stringify } from "qs";
import type { ResultMessage } from "./types";

const repeatArrayParamsSerializer = {
  serialize: params => stringify(params, { arrayFormat: "repeat" })
};

/** 获取后台账号分页列表 */
export const getAdminUserPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/passport/user/getByCondition",
    { params }
  );
};

/** 新增后台账号 */
export const createAdminUser = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/passport/user/register",
    {
      params,
      paramsSerializer: repeatArrayParamsSerializer
    }
  );
};

/** 修改后台账号 */
export const updateAdminUser = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/passport/user/admin/edit",
    {
      params,
      paramsSerializer: repeatArrayParamsSerializer
    }
  );
};

/** 启用/停用后台账号 */
export const updateAdminUserStatus = (userId: string, status: boolean) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/passport/user/enable/${userId}`,
    { params: { status } }
  );
};

/** 删除后台账号 */
export const deleteAdminUsers = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/passport/user/${ids.join(",")}`
  );
};

/** 重置后台账号密码 */
export const resetAdminUserPassword = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "post",
    `/manager/passport/user/resetPassword/${ids.join(",")}`
  );
};

/** 上传后台账号头像文件 */
export const uploadAdminUserAvatar = (data: FormData) => {
  return http.request<ResultMessage<string>>(
    "post",
    "/common/common/upload/file",
    { data },
    {
      headers: {
        "Content-Type": "multipart/form-data"
      }
    }
  );
};

/** 获取后台账号部门树 */
export const getAdminDepartmentTree = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/permission/department",
    { params }
  );
};

/** 获取后台账号角色列表 */
export const getAdminRolePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/permission/role",
    { params }
  );
};

/** 获取平台角色分页列表 */
export const getManagerRolePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/permission/role",
    { params }
  );
};

/** 新增平台角色 */
export const createManagerRole = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/permission/role", {
    params
  });
};

/** 修改平台角色 */
export const updateManagerRole = (
  roleId: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/permission/role/${roleId}`,
    { params }
  );
};

/** 删除平台角色 */
export const deleteManagerRoles = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/permission/role/${ids.join(",")}`
  );
};

/** 获取平台角色菜单授权 */
export const getManagerRoleMenuList = (roleId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/permission/roleMenu/${roleId}`
  );
};

/** 保存平台角色菜单授权 */
export const saveManagerRoleMenuList = (
  roleId: string,
  data: Array<Record<string, any>>
) => {
  return http.request<ResultMessage<any>>(
    "post",
    `/manager/permission/roleMenu/${roleId}`,
    { data }
  );
};

/** 获取平台部门树 */
export const getManagerDepartmentTree = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/permission/department",
    { params }
  );
};

/** 新增平台部门 */
export const createManagerDepartment = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>(
    "post",
    "/manager/permission/department",
    { params }
  );
};

/** 修改平台部门 */
export const updateManagerDepartment = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/permission/department/${id}`,
    { params }
  );
};

/** 删除平台部门 */
export const deleteManagerDepartments = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/permission/department/${ids.join(",")}`
  );
};

/** 获取平台菜单列表 */
export const getManagerMenuList = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/permission/menu",
    { params }
  );
};

/** 获取平台菜单树 */
export const getManagerMenuTree = () => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/permission/menu/tree"
  );
};

/** 新增平台菜单 */
export const createManagerMenu = (params?: Record<string, any>) => {
  return http.request<ResultMessage<any>>("post", "/manager/permission/menu", {
    params
  });
};

/** 修改平台菜单 */
export const updateManagerMenu = (
  id: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<any>>(
    "put",
    `/manager/permission/menu/${id}`,
    { params }
  );
};

/** 删除平台菜单 */
export const deleteManagerMenus = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/permission/menu/${ids.join(",")}`
  );
};

/** 获取平台操作日志分页 */
export const getManagerOperationLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/setting/log/getAllByPage",
    { params }
  );
};

/** 批量删除平台操作日志 */
export const deleteManagerOperationLogs = (ids: string[]) => {
  return http.request<ResultMessage<any>>(
    "delete",
    `/manager/setting/log/${ids.join(",")}`
  );
};

/** 清空平台操作日志 */
export const clearManagerOperationLogs = () => {
  return http.request<ResultMessage<any>>("delete", "/manager/setting/log");
};
