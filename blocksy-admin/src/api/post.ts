import http from "./http";

export interface PostMediaItem {
  objectKey: string;
  url: string;
  size: number;
}

export interface PostItem {
  id: number;
  communityId: number;
  content: string;
  authorId: number;
  commentCount: number;
  likeCount: number;
  createdAt: string;
  media: PostMediaItem[];
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchPosts(): Promise<PostItem[]> {
  const response = await http.get<ApiResponse<PostItem[]>>("/posts");
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取帖子失败");
  }
  return response.data.data || [];
}
