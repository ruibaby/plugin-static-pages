// Copied from https://github.com/halo-dev/halo/blob/main/ui/src/utils/api-client.ts
// TODO: https://github.com/halo-dev/halo/issues/3979

import type { AxiosError } from "axios";
import axios from "axios";
import { Toast } from "@halo-dev/components";
import QueryString from "qs";

const apiClient = axios.create({
  withCredentials: true,
  paramsSerializer: (params) => {
    return QueryString.stringify(params, { arrayFormat: "repeat" });
  },
});

export interface ProblemDetail {
  detail: string;
  instance: string;
  status: number;
  title: string;
  type?: string;
}

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError<ProblemDetail>) => {
    if (error.code === "ERR_CANCELED") {
      return Promise.reject(error);
    }

    if (/Network Error/.test(error.message)) {
      // @ts-ignore
      Toast.error("网络错误，请检查网络连接");
      return Promise.reject(error);
    }

    const errorResponse = error.response;

    if (!errorResponse) {
      Toast.error("网络错误，请检查网络连接");
      return Promise.reject(error);
    }

    // Don't show error toast
    // see https://github.com/halo-dev/halo/issues/2836
    if (errorResponse.config.mute) {
      return Promise.reject(error);
    }

    const { status } = errorResponse;
    const { title, detail } = errorResponse.data;

    if (status === 401) {
      Toast.warning("登录已过期，请重新登录");
      return Promise.reject(error);
    }

    if (title || detail) {
      Toast.error(detail || title);
      return Promise.reject(error);
    }

    // Final fallback
    if (errorResponse.status) {
      const { status, statusText } = errorResponse;
      Toast.error([status, statusText].filter(Boolean).join(": "));
      return Promise.reject(error);
    }

    Toast.error("未知错误");

    return Promise.reject(error);
  }
);

apiClient.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";

export { apiClient };
