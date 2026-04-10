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

export interface EventSignupItem {
  signupId: number;
  eventId: number;
  eventTitle?: string;
  communityId?: number;
  userId: number;
  remark?: string;
  status: number;
  signupAt: string;
}

export async function fetchEventSignups(params: {
  eventId?: number;
  communityId?: number;
  userId?: number;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<EventSignupItem>> {
  const { data } = await http.get<ApiResponse<PagedData<EventSignupItem>>>("/admin/events/signups", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}
