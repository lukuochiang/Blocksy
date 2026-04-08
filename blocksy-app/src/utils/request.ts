import { getStorage } from "./storage";

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const API_BASE_URL = "http://localhost:8080/api";

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  data?: unknown;
  auth?: boolean;
  headers?: Record<string, string>;
}

export function buildApiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

export function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", data, auth = false, headers = {} } = options;
  const requestHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...headers
  };
  if (auth) {
    const token = getStorage<string>("blocksy_token");
    if (token) {
      requestHeaders.Authorization = `Bearer ${token}`;
    }
  }
  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: buildApiUrl(path),
      method,
      data,
      header: requestHeaders,
      success: (res) => {
        const body = res.data as ApiResponse<T>;
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 0) {
          resolve(body.data);
          return;
        }
        reject(new Error(body?.message || "请求失败"));
      },
      fail: (error) => reject(error)
    });
  });
}
