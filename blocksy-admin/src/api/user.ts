import http from "./http";

export interface UserItem {
  id: number;
  username: string;
  nickname?: string;
  status: number;
  banReason?: string;
  bannedUntil?: string;
  bannedAt?: string;
  createdAt: string;
}

export interface UserPunishLogItem {
  id: number;
  userId: number;
  operatorUserId?: number;
  action: "BAN" | "UNBAN";
  reason?: string;
  durationHours?: number;
  expiresAt?: string;
  createdAt: string;
}

export interface BanUserPayload {
  reason?: string;
  durationHours?: number;
}

export interface UnbanUserPayload {
  reason?: string;
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

export async function fetchBlacklistedUsers(): Promise<UserItem[]> {
  const response = await http.get<ApiResponse<UserItem[]>>("/admin/users/blacklist");
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取黑名单用户失败");
  }
  return response.data.data || [];
}

export async function banUser(userId: number, payload: BanUserPayload = {}): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/users/${userId}/ban`, payload);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "封禁失败");
  }
}

export async function unbanUser(userId: number, payload: UnbanUserPayload = {}): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/users/${userId}/unban`, payload);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "解封失败");
  }
}

export async function fetchUserPunishLogs(userId: number): Promise<UserPunishLogItem[]> {
  const response = await http.get<ApiResponse<UserPunishLogItem[]>>(`/admin/users/${userId}/punish-logs`);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取处罚日志失败");
  }
  return response.data.data || [];
}
