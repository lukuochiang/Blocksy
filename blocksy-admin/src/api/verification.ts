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

export interface VerificationItem {
  id: number;
  userId: number;
  verifyType: string;
  realName?: string;
  idCardMask?: string;
  materialUrls?: string;
  processStatus: string;
  reviewNote?: string;
  reviewerUserId?: number;
  reviewedAt?: string;
  createdAt: string;
}

export async function fetchVerifications(params: {
  processStatus?: string;
  userId?: number;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<VerificationItem>> {
  const { data } = await http.get<ApiResponse<PagedData<VerificationItem>>>("/admin/verifications", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function handleVerification(id: number, payload: { processStatus: string; reviewNote?: string }): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/verifications/${id}/handle`, payload);
  if (data.code !== 0) {
    throw new Error(data.message || "处理认证失败");
  }
}
