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

export interface CommunityEngagementItem {
  communityId: number;
  communityName: string;
  memberCount: number;
  postCount: number;
  commentCount: number;
  activeScore: number;
}

export interface ContentCategoryItem {
  id: number;
  module: string;
  code: string;
  name: string;
  sortNo: number;
  enabled: boolean;
  updatedAt: string;
}

export interface MediaAssetItem {
  id: number;
  postId: number;
  communityId?: number;
  userId?: number;
  objectKey: string;
  url: string;
  size: number;
  contentType?: string;
  status: number;
  createdAt: string;
}

export async function fetchCommunityEngagement(params: {
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<CommunityEngagementItem>> {
  const { data } = await http.get<ApiResponse<PagedData<CommunityEngagementItem>>>("/admin/content/communities/engagement", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function fetchContentCategories(params: {
  module?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<ContentCategoryItem>> {
  const { data } = await http.get<ApiResponse<PagedData<ContentCategoryItem>>>("/admin/content/categories", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function createContentCategory(payload: {
  module: string;
  code: string;
  name: string;
  sortNo?: number;
}): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>("/admin/content/categories", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "新增分类失败");
  }
}

export async function toggleContentCategory(id: number, enabled: boolean): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/content/categories/${id}/toggle`, { enabled });
  if (data.code !== 0) {
    throw new Error(data.message || "更新分类状态失败");
  }
}

export async function fetchPostMediaAssets(params: {
  communityId?: number;
  postId?: number;
  status?: number;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<MediaAssetItem>> {
  const { data } = await http.get<ApiResponse<PagedData<MediaAssetItem>>>("/admin/content/media/posts", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function offlinePostMediaAsset(id: number): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/content/media/posts/${id}/offline`);
  if (data.code !== 0) {
    throw new Error(data.message || "下架媒体失败");
  }
}
