import http from "./http";

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  userId: number;
  username: string;
  token: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function login(params: LoginParams): Promise<LoginResult> {
  const response = await http.post<ApiResponse<LoginResult>>("/auth/login", params);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "登录失败");
  }
  return response.data.data;
}
