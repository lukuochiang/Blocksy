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

export interface CommunityNoticeItem {
  id: number;
  communityId: number;
  title: string;
  content: string;
  status: number;
  createdBy?: number;
  revokedBy?: number;
  revokedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export async function fetchCommunityNotices(params: {
  communityId?: number;
  status?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<CommunityNoticeItem>> {
  const { data } = await http.get<ApiResponse<PagedData<CommunityNoticeItem>>>("/admin/communities/notices", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function createCommunityNotice(payload: {
  communityId: number;
  title: string;
  content: string;
}): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>("/admin/communities/notices", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "发布社区公告失败");
  }
}

export async function revokeCommunityNotice(id: number): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/communities/notices/${id}/revoke`);
  if (data.code !== 0) {
    throw new Error(data.message || "撤回社区公告失败");
  }
}
