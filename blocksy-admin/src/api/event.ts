import http from "./http";

export interface EventItem {
  id: number;
  userId: number;
  communityId: number;
  title: string;
  content: string;
  startTime: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface EventHandleLog {
  id: number;
  eventId: number;
  operatorUserId: number;
  action: string;
  note?: string;
  createdAt: string;
}

export interface EventHandleLogQuery {
  eventId?: number;
  operatorUserId?: number;
  action?: string;
  startAt?: string;
  endAt?: string;
}

export interface EventBatchHandleResultItem {
  eventId: number | null;
  message: string;
}

export interface EventBatchHandleResponse {
  totalCount: number;
  successCount: number;
  successIds: number[];
  skippedIds: number[];
  failedItems: EventBatchHandleResultItem[];
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchAdminEvents(params?: {
  status?: number;
  communityId?: number;
  keyword?: string;
}): Promise<EventItem[]> {
  const { data } = await http.get<ApiResponse<EventItem[]>>("/admin/events", { params });
  return data.data ?? [];
}

export async function handleAdminEvent(
  id: number,
  payload: { action: "OFFLINE" | "RESTORE" | "DELETE"; note?: string }
): Promise<EventItem> {
  const { data } = await http.post<ApiResponse<EventItem>>(`/admin/events/${id}/handle`, payload);
  return data.data;
}

export async function fetchAdminEventLogs(id: number): Promise<EventHandleLog[]> {
  const { data } = await http.get<ApiResponse<EventHandleLog[]>>(`/admin/events/${id}/logs`);
  return data.data ?? [];
}

export async function fetchAdminEventHandleLogs(params?: EventHandleLogQuery): Promise<EventHandleLog[]> {
  const { data } = await http.get<ApiResponse<EventHandleLog[]>>("/admin/events/handle-logs", { params });
  return data.data ?? [];
}

export async function batchHandleAdminEvents(payload: {
  eventIds: number[];
  action: "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<EventBatchHandleResponse> {
  const { data } = await http.post<ApiResponse<EventBatchHandleResponse>>("/admin/events/batch-handle", payload);
  return data.data;
}

export async function retryBatchHandleAdminEvents(payload: {
  failedEventIds: number[];
  action: "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<EventBatchHandleResponse> {
  const { data } = await http.post<ApiResponse<EventBatchHandleResponse>>("/admin/events/batch-retry", payload);
  return data.data;
}

export async function retryBatchHandleAdminEventsExport(payload: {
  failedEventIds: number[];
  action: "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<Blob> {
  const response = await http.post("/admin/events/batch-retry/export", payload, {
    responseType: "blob"
  });
  return response.data as Blob;
}

export async function exportEventHandleLogsCsv(params?: EventHandleLogQuery): Promise<Blob> {
  const response = await http.get("/admin/events/handle-logs/export", {
    params,
    responseType: "blob"
  });
  return response.data as Blob;
}
