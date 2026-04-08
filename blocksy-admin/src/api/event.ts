import http from "./http";

export interface EventItem {
  id: number;
  title: string;
  description: string;
  startTime: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchEvents(): Promise<EventItem[]> {
  const { data } = await http.get<ApiResponse<EventItem[]>>("/events");
  return data.data ?? [];
}
