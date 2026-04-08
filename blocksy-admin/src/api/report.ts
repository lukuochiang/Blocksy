import http from "./http";

export interface ReportItem {
  id: number;
  reporterUserId: number;
  targetType: string;
  targetId: number;
  reason: string;
  processStatus: string;
  handlerUserId?: number;
  handledAt?: string;
  createdAt: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchReports(processStatus?: string): Promise<ReportItem[]> {
  const response = await http.get<ApiResponse<ReportItem[]>>("/admin/reports", {
    params: { processStatus }
  });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取举报失败");
  }
  return response.data.data || [];
}

export async function handleReport(reportId: number, action: "RESOLVED" | "REJECTED", banTargetUser = false): Promise<void> {
  const response = await http.post<ApiResponse<unknown>>(`/admin/reports/${reportId}/handle`, {
    action,
    banTargetUser
  });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "处理举报失败");
  }
}
