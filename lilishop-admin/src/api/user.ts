import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export interface LoginParams {
  username: string;
  password: string;
}

export interface MobileLoginParams {
  mobile: string;
  code: string;
  uuid: string;
}

export interface ResetPasswordParams {
  mobile: string;
  code: string;
  password: string;
  uuid: string;
}

export interface UserToken {
  accessToken: string;
  refreshToken: string;
}

export interface UserResult extends ResultMessage<UserToken> {}

export interface RefreshTokenResult extends ResultMessage<UserToken> {}

export type UserInfo = {
  id?: string;
  avatar: string;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  description: string;
  isSuper?: boolean;
  roleIds?: string;
};

type RawUserInfo = Partial<UserInfo> & {
  nickName?: string;
  mobile?: string;
};

export interface UserInfoResult extends ResultMessage<UserInfo> {}

type ResultTable = {
  code: number;
  message: string;
  data?: {
    list: Array<any>;
    total?: number;
    pageSize?: number;
    currentPage?: number;
  };
};

export const getLogin = (data: LoginParams) => {
  return http.request<UserResult>(
    "post",
    "/manager/passport/user/login",
    {
      params: {
        username: data.username,
        password: data.password
      }
    }
  );
};

export const sendLoginSmsCode = (mobile: string, uuid: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/passport/user/sms/${mobile}`,
    {
      headers: {
        uuid
      }
    }
  );
};

export const mobileLoginApi = (data: MobileLoginParams) => {
  return http.request<UserResult>("post", "/manager/passport/user/mobile/login", {
    params: {
      mobile: data.mobile,
      code: data.code
    },
    headers: {
      uuid: data.uuid
    }
  });
};

export const sendResetSmsCode = (mobile: string, uuid: string) => {
  return http.request<ResultMessage<any>>(
    "get",
    `/manager/passport/user/reset/sms/${mobile}`,
    {
      headers: {
        uuid
      }
    }
  );
};

export const verifyResetSmsCode = (mobile: string, code: string, uuid: string) => {
  return http.request<ResultMessage<any>>("post", "/manager/passport/user/reset/verify", {
    params: {
      mobile,
      code
    },
    headers: {
      uuid
    }
  });
};

export const resetPasswordApi = (data: ResetPasswordParams) => {
  return http.request<ResultMessage<any>>("post", "/manager/passport/user/reset/password", {
    params: {
      mobile: data.mobile,
      password: data.password
    },
    headers: {
      uuid: data.uuid
    }
  });
};

export const refreshTokenApi = (refreshToken: string) => {
  return http.request<RefreshTokenResult>(
    "get",
    `/manager/passport/user/refresh/${refreshToken}`
  );
};

export const getMine = () => {
  return http.request<UserInfoResult>("get", "/manager/passport/user/info");
};

export function normalizeUserInfo(payload?: RawUserInfo): UserInfo {
  return {
    id: payload?.id,
    avatar: payload?.avatar || "",
    username: payload?.username || "",
    nickname: payload?.nickname || payload?.nickName || "",
    email: payload?.email || "",
    phone: payload?.phone || payload?.mobile || "",
    description: payload?.description || "",
    isSuper: payload?.isSuper,
    roleIds: payload?.roleIds
  };
}

export const updateMineProfile = (data: {
  avatar?: string;
  nickName?: string;
  email?: string;
  mobile?: string;
  description?: string;
}) => {
  return http.request<ResultMessage<any>>("put", "/manager/passport/user/edit", {
    params: data
  });
};

export const editPasswordApi = (password: string, newPassword: string) => {
  return http.request<ResultMessage<any>>(
    "put",
    "/manager/passport/user/editPassword",
    {
      params: {
        password,
        newPassword
      }
    }
  );
};

export const logoutApi = () => {
  return http.request<ResultMessage<any>>("post", "/manager/passport/user/logout");
};

export const getMineLogs = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/setting/log/getAllByPage",
    { params }
  );
};
