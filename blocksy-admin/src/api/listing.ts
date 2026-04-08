import http from "./http";

export interface ListingItem {
  id: number;
  title: string;
  description: string;
  price: number | null;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchListings(): Promise<ListingItem[]> {
  const { data } = await http.get<ApiResponse<ListingItem[]>>("/listings");
  return data.data ?? [];
}
