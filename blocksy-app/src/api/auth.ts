import { request } from "../utils/request";

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  userId: number;
  username: string;
  token: string;
}

export function login(params: LoginParams): Promise<LoginResult> {
  return request<LoginResult>("/auth/login", {
    method: "POST",
    data: params
  });
}
