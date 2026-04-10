import http from "./http";

export interface ListingItem {
  id: number;
  userId: number;
  communityId: number;
  category: string;
  title: string;
  content: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface ListingHandleLog {
  id: number;
  listingId: number;
  operatorUserId: number;
  action: string;
  note?: string;
  createdAt: string;
}

export interface ListingCategoryStats {
  category: string;
  totalCount: number;
  pendingCount: number;
  onlineCount: number;
  offlineCount: number;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

interface PageResponse<T> {
  page: number;
  pageSize: number;
  total: number;
  items: T[];
}

export async function fetchAdminListings(params?: {
  status?: number;
  communityId?: number;
  category?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PageResponse<ListingItem>> {
  const { data } = await http.get<ApiResponse<PageResponse<ListingItem>>>("/admin/listings", { params });
  return data.data ?? { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function handleAdminListing(
  id: number,
  payload: { action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE"; note?: string }
): Promise<ListingItem> {
  const { data } = await http.post<ApiResponse<ListingItem>>(`/admin/listings/${id}/handle`, payload);
  return data.data;
}

export async function batchHandleAdminListings(payload: {
  listingIds: number[];
  action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<{ total: number; successCount: number; failCount: number; items: Array<{ listingId: number; success: boolean; message: string }> }> {
  const { data } = await http.post<ApiResponse<{ total: number; successCount: number; failCount: number; items: Array<{ listingId: number; success: boolean; message: string }> }>>(
    "/admin/listings/batch-handle",
    payload
  );
  return data.data;
}

export async function retryBatchAdminListings(payload: {
  failedListingIds: number[];
  action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<{ total: number; successCount: number; failCount: number; items: Array<{ listingId: number; success: boolean; message: string }> }> {
  const { data } = await http.post<ApiResponse<{ total: number; successCount: number; failCount: number; items: Array<{ listingId: number; success: boolean; message: string }> }>>(
    "/admin/listings/batch-retry",
    payload
  );
  return data.data;
}

export async function exportRetryBatchAdminListings(payload: {
  failedListingIds: number[];
  action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE";
  note?: string;
}): Promise<Blob> {
  const { data } = await http.post("/admin/listings/batch-retry/export", payload, { responseType: "blob" });
  return data as Blob;
}

export async function fetchAdminListingCategoryStats(params?: {
  communityId?: number;
  days?: number;
  startDate?: string;
  endDate?: string;
}): Promise<ListingCategoryStats[]> {
  const { data } = await http.get<ApiResponse<ListingCategoryStats[]>>("/admin/listings/stats/category", {
    params
  });
  return data.data ?? [];
}

export async function fetchAdminListingLogs(id: number): Promise<ListingHandleLog[]> {
  const { data } = await http.get<ApiResponse<ListingHandleLog[]>>(`/admin/listings/${id}/logs`);
  return data.data ?? [];
}
