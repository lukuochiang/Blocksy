import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageData<T> {
  page: number;
  pageSize: number;
  total: number;
  items: T[];
}

export interface AdminPostItem {
  id: number;
  userId: number;
  communityId: number;
  content: string;
  status: number;
  commentCount: number;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface AdminPostQuery {
  status?: number;
  communityId?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export async function fetchAdminPosts(query: AdminPostQuery): Promise<PageData<AdminPostItem>> {
  const response = await http.get<ApiResponse<PageData<AdminPostItem>>>("/admin/posts", { params: query });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取帖子审核列表失败");
  }
  return response.data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function reviewAdminPost(postId: number, action: "APPROVE" | "REJECT"): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/posts/${postId}/review`, { action });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "帖子审核操作失败");
  }
}
