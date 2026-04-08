import { request } from "../utils/request";

export interface UserProfile {
  id: number;
  username: string;
  nickname?: string;
  avatarUrl?: string;
  status: number;
  defaultCommunityId?: number;
}

export function getCurrentUser(): Promise<UserProfile> {
  return request<UserProfile>("/users/me", { auth: true });
}

export interface UserCommunityItem {
  communityId: number;
  communityCode: string;
  communityName: string;
  isDefault: boolean;
}

export function getMyCommunities(): Promise<UserCommunityItem[]> {
  return request<UserCommunityItem[]>("/users/communities", { auth: true });
}

export function selectMyCommunity(communityId: number): Promise<void> {
  return request<void>("/users/community", {
    method: "PUT",
    auth: true,
    data: { communityId }
  });
}
