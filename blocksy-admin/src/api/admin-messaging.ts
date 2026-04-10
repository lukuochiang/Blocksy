import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PagedData<T> {
  page: number;
  pageSize: number;
  total: number;
  items: T[];
}

export interface PushTaskItem {
  id: number;
  title: string;
  content: string;
  targetType: string;
  taskStatus: string;
  sentAt?: string;
  createdBy?: number;
  createdAt: string;
}

export interface PushRecordItem {
  id: number;
  taskId: number;
  userId: number;
  channel: string;
  sendStatus: string;
  readStatus: boolean;
  deliveredAt?: string;
  readAt?: string;
  createdAt: string;
}

export interface NotificationTemplateItem {
  id: number;
  module: string;
  triggerCode: string;
  titleTemplate: string;
  contentTemplate: string;
  enabled: boolean;
  updatedAt: string;
}

export async function fetchPushTasks(params: {
  taskStatus?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<PushTaskItem>> {
  const { data } = await http.get<ApiResponse<PagedData<PushTaskItem>>>("/admin/notifications/push/tasks", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function createPushTask(payload: {
  title: string;
  content: string;
  targetType?: string;
}): Promise<PushTaskItem> {
  const { data } = await http.post<ApiResponse<PushTaskItem>>("/admin/notifications/push/tasks", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "创建推送任务失败");
  }
  return data.data;
}

export async function sendPushTask(taskId: number): Promise<number> {
  const { data } = await http.post<ApiResponse<number>>(`/admin/notifications/push/tasks/${taskId}/send`);
  if (data.code !== 0) {
    throw new Error(data.message || "下发任务失败");
  }
  return data.data || 0;
}

export async function fetchPushRecords(params: {
  taskId?: number;
  sendStatus?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<PushRecordItem>> {
  const { data } = await http.get<ApiResponse<PagedData<PushRecordItem>>>("/admin/notifications/push/records", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function fetchNotificationTemplates(params: {
  module?: string;
  enabled?: boolean;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<NotificationTemplateItem>> {
  const { data } = await http.get<ApiResponse<PagedData<NotificationTemplateItem>>>("/admin/notifications/templates", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function saveNotificationTemplate(payload: {
  module: string;
  triggerCode: string;
  titleTemplate: string;
  contentTemplate: string;
  enabled: boolean;
}): Promise<NotificationTemplateItem> {
  const { data } = await http.post<ApiResponse<NotificationTemplateItem>>("/admin/notifications/templates/save", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "保存模板失败");
  }
  return data.data;
}
