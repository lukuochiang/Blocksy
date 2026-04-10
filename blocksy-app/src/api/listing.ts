import { request } from "../utils/request";

export type ListingCategory = "SECOND_HAND" | "LOST_FOUND" | "HELP_WANTED";

export interface ListingItem {
  id: number;
  communityId: number;
  userId: number;
  category: ListingCategory;
  title: string;
  content: string;
  price?: number;
  contact?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  status: number;
  createdAt?: string;
}

export interface ListingStatusLogItem {
  id: number;
  listingId: number;
  operatorUserId: number;
  action: string;
  note?: string;
  createdAt: string;
}

export interface CreateListingParams {
  communityId: number;
  category: ListingCategory;
  title: string;
  content: string;
  price?: number;
  contact?: string;
  coverObjectKey?: string;
  coverUrl?: string;
}

export interface ListingListQuery {
  communityId?: number;
  category?: ListingCategory | "ALL";
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: "CREATED_AT" | "PRICE";
  sortOrder?: "ASC" | "DESC";
}

function buildQuery(params: Record<string, string | number | undefined>): string {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return query ? `?${query}` : "";
}

export function getListingList(query?: ListingListQuery): Promise<ListingItem[]> {
  return request<ListingItem[]>(`/listings${buildQuery(query ?? {})}`);
}

export function getListingDetail(id: number): Promise<ListingItem> {
  return request<ListingItem>(`/listings/${id}`);
}

export function getListingRecommendations(id: number, limit = 6): Promise<ListingItem[]> {
  return request<ListingItem[]>(`/listings/${id}/recommendations${buildQuery({ limit })}`);
}

export function getMyListingDetail(id: number): Promise<ListingItem> {
  return request<ListingItem>(`/listings/mine/${id}`, { auth: true });
}

export function getMyListings(communityId?: number, status?: number): Promise<ListingItem[]> {
  return request<ListingItem[]>(`/listings/mine${buildQuery({ communityId, status })}`, { auth: true });
}

export function createListing(payload: CreateListingParams): Promise<ListingItem> {
  return request<ListingItem>("/listings", {
    method: "POST",
    auth: true,
    data: payload
  });
}

export function getMyListingLogs(id: number): Promise<ListingStatusLogItem[]> {
  return request<ListingStatusLogItem[]>(`/listings/mine/${id}/logs`, { auth: true });
}

export function offlineMyListing(id: number, note?: string): Promise<ListingItem> {
  return request<ListingItem>(`/listings/mine/${id}/offline${buildQuery({ note })}`, {
    method: "POST",
    auth: true
  });
}

export function resubmitMyListing(id: number, note?: string): Promise<ListingItem> {
  return request<ListingItem>(`/listings/mine/${id}/resubmit${buildQuery({ note })}`, {
    method: "POST",
    auth: true
  });
}

export function deleteMyListing(id: number, note?: string): Promise<ListingItem> {
  return request<ListingItem>(`/listings/mine/${id}/delete${buildQuery({ note })}`, {
    method: "POST",
    auth: true
  });
}
