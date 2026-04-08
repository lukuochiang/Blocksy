import http from "./http";

export interface UserItem {
  id: number;
  username: string;
  nickname?: string;
  status: number;
  createdAt: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchUsers(): Promise<UserItem[]> {
  const response = await http.get<ApiResponse<UserItem[]>>("/admin/users");
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取用户失败");
  }
  return response.data.data || [];
}

export async function banUser(userId: number): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/users/${userId}/ban`);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "封禁失败");
  }
}

export async function unbanUser(userId: number): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/users/${userId}/unban`);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "解封失败");
  }
}
