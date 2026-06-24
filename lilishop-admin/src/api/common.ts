import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

/** 通用文件上传 */
export const uploadCommonFile = (data: FormData) => {
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
