import { request } from "../utils/request";

export interface PostMedia {
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
  media: PostMedia[];
}

export interface CreatePostParams {
  communityId: number;
  content: string;
  media: PostMedia[];
}

export interface CreateCommentParams {
  postId: number;
  content: string;
  replyToCommentId?: number;
}

export interface PostPageData {
  page: number;
  pageSize: number;
  total: number;
  items: PostItem[];
}

export interface PostPageQuery {
  communityId?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

function buildQuery(params: Record<string, number | string | undefined>): string {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return query ? `?${query}` : "";
}

export function getPostList(query: PostPageQuery = {}): Promise<PostPageData> {
  return request<PostPageData>(`/posts${buildQuery(query)}`);
}

export function getPostDetail(postId: number): Promise<PostItem> {
  return request<PostItem>(`/posts/${postId}`);
}

export function createPost(payload: CreatePostParams): Promise<PostItem> {
  return request<PostItem>("/posts", {
    method: "POST",
    data: payload,
    auth: true
  });
}

export function createComment(payload: CreateCommentParams): Promise<void> {
  return request<void>("/comments", {
    method: "POST",
    data: payload,
    auth: true
  });
}

export function getMyPosts(query: PostPageQuery = {}): Promise<PostPageData> {
  return request<PostPageData>(`/posts/mine${buildQuery(query)}`, { auth: true });
}
