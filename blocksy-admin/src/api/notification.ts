import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface NotificationTypeStatItem {
  type: string;
  totalCount: number;
  readCount: number;
  unreadCount: number;
}

export interface NotificationStats {
  totalCount: number;
  readCount: number;
  unreadCount: number;
  todayCount: number;
  readRate: number;
  typeStats: NotificationTypeStatItem[];
}

export interface GovernanceStats {
  pendingReports: number;
  handledReportsToday: number;
  avgHandleHours7d: number;
  repeatPunishedUsers: number;
}

export interface AnnouncementItem {
  id: number;
  title: string;
  content: string;
  status: number;
  dispatchCount: number;
  lastDispatchedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface NotificationTrendPoint {
  day: string;
  totalCount: number;
  readCount: number;
  readRate: number;
}

export interface PagedData<T> {
  page: number;
  pageSize: number;
  total: number;
  items: T[];
}

export async function publishSystemAnnouncement(payload: { title: string; content: string }): Promise<number> {
  const { data } = await http.post<ApiResponse<number>>("/admin/notifications/system-announcement", payload);
  return data.data;
}

export async function fetchNotificationStats(): Promise<NotificationStats> {
  const { data } = await http.get<ApiResponse<NotificationStats>>("/admin/notifications/stats");
  return data.data;
}

export async function fetchGovernanceStats(): Promise<GovernanceStats> {
  const { data } = await http.get<ApiResponse<GovernanceStats>>("/admin/notifications/governance-stats");
  return data.data;
}

export async function fetchAnnouncements(params: {
  keyword?: string;
  status?: number;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<AnnouncementItem>> {
  const { data } = await http.get<ApiResponse<PagedData<AnnouncementItem>>>("/admin/notifications/announcements", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function revokeAnnouncement(id: number): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/notifications/${id}/revoke`);
  if (data.code !== 0) {
    throw new Error(data.message || "撤回失败");
  }
}

export async function redispatchAnnouncement(id: number): Promise<number> {
  const { data } = await http.post<ApiResponse<number>>(`/admin/notifications/${id}/redispatch`);
  if (data.code !== 0) {
    throw new Error(data.message || "二次下发失败");
  }
  return data.data || 0;
}

export async function fetchNotificationTrend(days: 7 | 30): Promise<NotificationTrendPoint[]> {
  const { data } = await http.get<ApiResponse<NotificationTrendPoint[]>>("/admin/notifications/trend", {
    params: { days }
  });
  return data.data || [];
}
