import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface AdminCommentItem {
  id: number;
  postId: number;
  userId: number;
  content: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface AdminCommentQuery {
  postId?: number;
  status?: number;
  keyword?: string;
}

export async function fetchAdminComments(query: AdminCommentQuery): Promise<AdminCommentItem[]> {
  const response = await http.get<ApiResponse<AdminCommentItem[]>>("/admin/comments", { params: query });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取评论列表失败");
  }
  return response.data.data || [];
}

export async function deleteAdminComment(commentId: number): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/comments/${commentId}/delete`);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "删除评论失败");
  }
}
