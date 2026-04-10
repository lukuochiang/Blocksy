import { request } from "../utils/request";

export interface EventItem {
  id: number;
  communityId: number;
  userId: number;
  title: string;
  content: string;
  location?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  startTime: string;
  endTime?: string;
  signupLimit?: number;
  signupCount?: number;
  status: number;
}

export interface CreateEventParams {
  communityId: number;
  title: string;
  content: string;
  location?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  startTime: string;
  endTime?: string;
  signupLimit?: number;
}

function buildQuery(params: Record<string, string | number | undefined>): string {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return query ? `?${query}` : "";
}

export function getEventList(communityId?: number): Promise<EventItem[]> {
  return request<EventItem[]>(`/events${buildQuery({ communityId })}`);
}

export function getEventDetail(id: number): Promise<EventItem> {
  return request<EventItem>(`/events/${id}`);
}

export function getMyEvents(communityId?: number): Promise<EventItem[]> {
  return request<EventItem[]>(`/events/mine${buildQuery({ communityId })}`, { auth: true });
}

export function getMyEventSignups(): Promise<EventItem[]> {
  return request<EventItem[]>("/events/my-signups", { auth: true });
}

export function createEvent(payload: CreateEventParams): Promise<EventItem> {
  return request<EventItem>("/events", {
    method: "POST",
    auth: true,
    data: payload
  });
}

export function signupEvent(id: number, remark?: string): Promise<void> {
  return request<void>(`/events/${id}/signup`, {
    method: "POST",
    auth: true,
    data: { remark }
  });
}
