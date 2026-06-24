const SUCCESS_CODES = new Set([0, 200]);

export interface ApiResult<T = unknown> {
  code?: number;
  success?: boolean;
  data?: T;
  result?: T;
}

export function isSuccessResult<T = unknown>(payload?: ApiResult<T>) {
  return payload?.success === true || SUCCESS_CODES.has(payload?.code);
}

export function unwrapResult<T = unknown>(payload?: ApiResult<T>) {
  return payload?.data ?? payload?.result;
}
