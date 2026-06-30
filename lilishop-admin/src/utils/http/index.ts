import Axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type CustomParamsSerializer
} from "axios";
import type {
  PureHttpError,
  RequestMethods,
  PureHttpResponse,
  PureHttpRequestConfig
} from "./types.d";
import { stringify } from "qs";
import { message } from "@/utils/message";
import { recordRecentRequestError } from "@/utils/error";
import { $t, transformI18n } from "@/plugins/i18n";
import { getToken, formatToken } from "@/utils/auth";
import { useUserStoreHook } from "@/store/modules/user";

// 相关配置请参考：www.axios-js.com/zh-cn/docs/#axios-request-config-1
const defaultConfig: AxiosRequestConfig = {
  // 请求超时时间
  timeout: 10000,
  headers: {
    Accept: "application/json, text/plain, */*",
    "Content-Type": "application/json",
    "X-Requested-With": "XMLHttpRequest"
  },
  // 数组格式参数序列化（https://github.com/axios/axios/issues/5142）
  paramsSerializer: {
    serialize: stringify as unknown as CustomParamsSerializer
  }
};

function isNonEmptyString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function extractErrorPayload(data: unknown) {
  if (!data) return null;
  if (typeof data === "string") {
    try {
      return JSON.parse(data);
    } catch {
      return {
        message: data
      };
    }
  }
  return typeof data === "object" ? data : null;
}

function resolveHttpErrorMessage(error: PureHttpError) {
  const payload = extractErrorPayload(error.response?.data) as
    | {
        message?: unknown;
        msg?: unknown;
        error?: unknown;
      }
    | null;
  const serverMessage = [
    payload?.message,
    payload?.msg,
    payload?.error
  ].find(isNonEmptyString);

  if (serverMessage) {
    return serverMessage;
  }

  if (error.code === "ECONNABORTED") {
    return "请求超时，请稍后重试";
  }

  if (error.message === "Network Error") {
    return "网络异常，请检查服务或网络连接";
  }

  const status = error.response?.status;
  if (status) {
    const statusMessageMap: Record<number, string> = {
      400: "请求参数错误",
      401: "登录状态已失效，请重新登录",
      403: "没有权限执行该操作",
      404: "请求的接口不存在",
      405: "请求方法不被允许",
      408: "请求超时，请稍后重试",
      422: "提交的数据校验未通过",
      429: "请求过于频繁，请稍后再试",
      500: "服务异常，请稍后重试",
      502: "网关异常，请稍后重试",
      503: "服务暂不可用，请稍后重试",
      504: "网关超时，请稍后重试"
    };
    return statusMessageMap[status] || `请求失败（${status}）`;
  }

  return error.message;
}

function isPlainObject(value: unknown): value is Record<string, any> {
  return Object.prototype.toString.call(value) === "[object Object]";
}

function isBusinessSuccess(payload: Record<string, any>) {
  if (payload.success === true) {
    return true;
  }
  if (typeof payload.code === "number") {
    return payload.code === 0 || payload.code === 200;
  }
  return true;
}

function resolveBusinessErrorMessage(payload: Record<string, any>) {
  const serverMessage = [payload.message, payload.msg, payload.error].find(
    isNonEmptyString
  );
  return serverMessage || "请求处理失败";
}

function buildBusinessError(
  response: PureHttpResponse,
  payload: Record<string, any>
): PureHttpError {
  const error = new Error(resolveBusinessErrorMessage(payload)) as PureHttpError;
  error.name = "BusinessError";
  error.config = response.config as any;
  error.response = response as any;
  return error;
}

class PureHttp {
  constructor() {
    this.httpInterceptorsRequest();
    this.httpInterceptorsResponse();
  }

  /** `token`过期后，暂存待执行的请求 */
  private static requests = [];

  /** 防止重复刷新`token` */
  private static isRefreshing = false;

  /** 初始化配置对象 */
  private static initConfig: PureHttpRequestConfig = {};

  /** 保存当前`Axios`实例对象 */
  private static axiosInstance: AxiosInstance = Axios.create(defaultConfig);

  /** 统一写入鉴权头，兼容当前后端只识别 `accessToken` 的实现 */
  private static setAuthHeaders(
    headers: PureHttpRequestConfig["headers"],
    token: string
  ) {
    headers["accessToken"] = token;
    headers["Authorization"] = formatToken(token);
  }

  /** 重连原始请求 */
  private static retryOriginalRequest(config: PureHttpRequestConfig) {
    return new Promise(resolve => {
      PureHttp.requests.push((token: string) => {
        PureHttp.setAuthHeaders(config.headers, token);
        resolve(config);
      });
    });
  }

  /** 请求拦截 */
  private httpInterceptorsRequest(): void {
    PureHttp.axiosInstance.interceptors.request.use(
      async (config: PureHttpRequestConfig): Promise<any> => {
        // 优先判断post/get等方法是否传入回调，否则执行初始化设置等回调
        if (typeof config.beforeRequestCallback === "function") {
          config.beforeRequestCallback(config);
          return config;
        }
        if (PureHttp.initConfig.beforeRequestCallback) {
          PureHttp.initConfig.beforeRequestCallback(config);
          return config;
        }
        /** 请求白名单，放置一些不需要`token`的接口（通过设置请求白名单，防止`token`过期后再请求造成的死循环问题） */
        const whiteList = [
          "/refresh-token",
          "/login",
          "/manager/passport/user/login",
          "/manager/passport/user/mobile/login",
          "/manager/passport/user/sms/",
          "/manager/passport/user/reset/",
          "/manager/passport/user/refresh/"
        ];
        return whiteList.some(url => config.url.endsWith(url))
          ? config
          : new Promise(resolve => {
              const data = getToken();
              if (data) {
                const now = new Date().getTime();
                const expired = parseInt(data.expires) - now <= 0;
                if (expired) {
                  if (!PureHttp.isRefreshing) {
                    PureHttp.isRefreshing = true;
                    // token过期刷新
                    useUserStoreHook()
                      .handRefreshToken({ refreshToken: data.refreshToken })
                      .then(res => {
                        const token = res.data.accessToken;
                        PureHttp.setAuthHeaders(config.headers, token);
                        PureHttp.requests.forEach(cb => cb(token));
                        PureHttp.requests = [];
                      })
                      .catch(_err => {
                        PureHttp.requests = [];
                        useUserStoreHook().logOut();
                        message(transformI18n($t("login.pureLoginExpired")), {
                          type: "warning"
                        });
                      })
                      .finally(() => {
                        PureHttp.isRefreshing = false;
                      });
                  }
                  resolve(PureHttp.retryOriginalRequest(config));
                } else {
                  PureHttp.setAuthHeaders(config.headers, data.accessToken);
                  resolve(config);
                }
              } else {
                resolve(config);
              }
            });
      },
      error => {
        return Promise.reject(error);
      }
    );
  }

  /** 响应拦截 */
  private httpInterceptorsResponse(): void {
    const instance = PureHttp.axiosInstance;
    instance.interceptors.response.use(
      (response: PureHttpResponse) => {
        const $config = response.config;
        const payload = response.data;
        if (
          $config.enableBusinessErrorReject !== false &&
          isPlainObject(payload) &&
          ("success" in payload || "code" in payload) &&
          !isBusinessSuccess(payload)
        ) {
          recordRecentRequestError(resolveBusinessErrorMessage(payload));
          return Promise.reject(buildBusinessError(response, payload));
        }
        // 优先判断post/get等方法是否传入回调，否则执行初始化设置等回调
        if (typeof $config.beforeResponseCallback === "function") {
          $config.beforeResponseCallback(response);
          return response.data;
        }
        if (PureHttp.initConfig.beforeResponseCallback) {
          PureHttp.initConfig.beforeResponseCallback(response);
          return response.data;
        }
        return response.data;
      },
      (error: PureHttpError) => {
        const $error = error;
        $error.isCancelRequest = Axios.isCancel($error);
        if (!$error.isCancelRequest) {
          $error.message = resolveHttpErrorMessage($error);
          recordRecentRequestError($error.message);
        }
        // 所有的响应异常 区分来源为取消请求/非取消请求
        return Promise.reject($error);
      }
    );
  }

  /** 通用请求工具函数 */
  public request<T>(
    method: RequestMethods,
    url: string,
    param?: AxiosRequestConfig,
    axiosConfig?: PureHttpRequestConfig
  ): Promise<T> {
    const config = {
      method,
      url,
      ...param,
      ...axiosConfig
    } as PureHttpRequestConfig;

    // 单独处理自定义请求/响应回调
    return new Promise((resolve, reject) => {
      PureHttp.axiosInstance
        .request(config)
        .then((response: undefined) => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  }

  /** 单独抽离的`post`工具函数 */
  public post<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig
  ): Promise<T> {
    return this.request<T>("post", url, params, config);
  }

  /** 单独抽离的`get`工具函数 */
  public get<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig
  ): Promise<T> {
    return this.request<T>("get", url, params, config);
  }
}

export const http = new PureHttp();
