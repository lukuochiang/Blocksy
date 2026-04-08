import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
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
}

export async function fetchAdminPosts(query: AdminPostQuery): Promise<AdminPostItem[]> {
  const response = await http.get<ApiResponse<AdminPostItem[]>>("/admin/posts", { params: query });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取帖子审核列表失败");
  }
  return response.data.data || [];
}

export async function reviewAdminPost(postId: number, action: "APPROVE" | "REJECT"): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/posts/${postId}/review`, { action });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "帖子审核操作失败");
  }
}
