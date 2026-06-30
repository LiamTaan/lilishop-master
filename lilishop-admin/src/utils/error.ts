const RECENT_REQUEST_ERROR_WINDOW_MS = 1500;

const GENERIC_ERROR_PATTERNS = [
  "失败",
  "异常",
  "错误",
  "重试",
  "请检查",
  "请确认",
  "契约"
];

let recentRequestError:
  | {
      message: string;
      timestamp: number;
    }
  | null = null;

function isNonEmptyString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function getRecentRequestErrorMessage() {
  if (!recentRequestError) return "";
  if (Date.now() - recentRequestError.timestamp > RECENT_REQUEST_ERROR_WINDOW_MS) {
    recentRequestError = null;
    return "";
  }
  return recentRequestError.message;
}

function isGenericErrorText(value: string) {
  return GENERIC_ERROR_PATTERNS.some(pattern => value.includes(pattern));
}

export function recordRecentRequestError(message?: unknown) {
  if (!isNonEmptyString(message)) return;
  recentRequestError = {
    message: message.trim(),
    timestamp: Date.now()
  };
}

export function extractErrorMessage(error: unknown) {
  if (error instanceof Error && isNonEmptyString(error.message)) {
    return error.message.trim();
  }
  if (isNonEmptyString(error)) {
    return error.trim();
  }
  if (
    typeof error === "object" &&
    error &&
    "message" in error &&
    isNonEmptyString((error as Record<string, unknown>).message)
  ) {
    return String((error as Record<string, unknown>).message).trim();
  }
  return "";
}

export function getErrorMessage(error: unknown, fallback: string) {
  return extractErrorMessage(error) || getRecentRequestErrorMessage() || fallback;
}

export function resolveMessageContent(message: unknown, type?: string) {
  if (type !== "error" || !isNonEmptyString(message)) {
    return message;
  }
  const text = message.trim();
  const recentMessage = getRecentRequestErrorMessage();
  if (!recentMessage || !isGenericErrorText(text) || recentMessage === text) {
    return message;
  }
  return recentMessage;
}
