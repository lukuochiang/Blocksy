import { request } from "../utils/request";

export interface CreateReportParams {
  targetType: "POST" | "LISTING" | "EVENT";
  targetId: number;
  reason: string;
  description?: string;
}

export function createReport(payload: CreateReportParams): Promise<void> {
  return request<void>("/reports", {
    method: "POST",
    auth: true,
    data: payload
  });
}
