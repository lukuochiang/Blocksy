import { request } from "../utils/request";

export interface NotificationItem {
  id: number;
  type: string;
  title?: string;
  content?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationUnreadCount {
  unreadCount: number;
}

export interface NotificationPageResponse {
  page: number;
  pageSize: number;
  total: number;
  items: NotificationItem[];
}

export function getNotificationList(): Promise<NotificationItem[]> {
  return request<NotificationItem[]>("/notifications", { auth: true });
}

export function getNotificationPage(params?: {
  page?: number;
  pageSize?: number;
  type?: "INTERACTION" | "SYSTEM" | "ACTIVITY";
  isRead?: boolean;
}): Promise<NotificationPageResponse> {
  const query = new URLSearchParams();
  if (params?.page != null) query.set("page", String(params.page));
  if (params?.pageSize != null) query.set("pageSize", String(params.pageSize));
  if (params?.type) query.set("type", params.type);
  if (params?.isRead != null) query.set("isRead", String(params.isRead));
  const queryText = query.toString();
  return request<NotificationPageResponse>(`/notifications/page${queryText ? `?${queryText}` : ""}`, { auth: true });
}

export function getNotificationUnreadCount(): Promise<NotificationUnreadCount> {
  return request<NotificationUnreadCount>("/notifications/unread-count", { auth: true });
}

export function markNotificationRead(id: number): Promise<boolean> {
  return request<boolean>(`/notifications/${id}/read`, { method: "POST", auth: true });
}

export function markNotificationsReadBatch(notificationIds: number[]): Promise<number> {
  return request<number>("/notifications/read-batch", {
    method: "POST",
    auth: true,
    data: { notificationIds }
  });
}

export function markAllNotificationsRead(): Promise<number> {
  return request<number>("/notifications/read-all", { method: "POST", auth: true });
}
