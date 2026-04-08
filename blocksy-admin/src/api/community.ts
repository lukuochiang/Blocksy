import http from "./http";

export interface CommunityItem {
  id: number;
  code: string;
  name: string;
  address?: string;
  description?: string;
  status: number;
  createdAt: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface CreateCommunityParams {
  code: string;
  name: string;
  address?: string;
  description?: string;
}

export async function fetchCommunities(): Promise<CommunityItem[]> {
  const response = await http.get<ApiResponse<CommunityItem[]>>("/admin/communities");
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取社区失败");
  }
  return response.data.data || [];
}

export async function createCommunity(payload: CreateCommunityParams): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>("/admin/communities", payload);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "创建社区失败");
  }
}
