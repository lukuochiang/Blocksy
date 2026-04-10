import http from "./http";

export interface ReportItem {
  id: number;
  reporterUserId: number;
  targetType: string;
  targetId: number;
  reason: string;
  processStatus: string;
  handlerUserId?: number;
  handlerNote?: string;
  handledAt?: string;
  createdAt: string;
}

export interface ReportHandleLogItem {
  id: number;
  reportId: number;
  operatorUserId: number;
  action: string;
  note?: string;
  banTargetUser: boolean;
  createdAt: string;
}

export interface ReportBatchItemResult {
  reportId?: number;
  message: string;
}

export interface ReportBatchHandleResult {
  totalCount: number;
  successCount: number;
  successIds: number[];
  skippedIds: number[];
  failedItems: ReportBatchItemResult[];
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

export async function batchHandleReport(
  reportIds: number[],
  action: "RESOLVED" | "REJECTED",
  banTargetUser = false,
  note?: string
): Promise<ReportBatchHandleResult> {
  const response = await http.post<ApiResponse<ReportBatchHandleResult>>("/admin/reports/batch-handle", {
    reportIds,
    action,
    banTargetUser,
    note
  });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "批量处理举报失败");
  }
  return response.data.data;
}

export async function retryBatchHandleReport(
  failedReportIds: number[],
  action: "RESOLVED" | "REJECTED",
  banTargetUser = false,
  note?: string
): Promise<ReportBatchHandleResult> {
  const response = await http.post<ApiResponse<ReportBatchHandleResult>>("/admin/reports/batch-retry", {
    failedReportIds,
    action,
    banTargetUser,
    note
  });
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "重试批量处理举报失败");
  }
  return response.data.data;
}

export async function retryBatchHandleReportExport(
  failedReportIds: number[],
  action: "RESOLVED" | "REJECTED",
  banTargetUser = false,
  note?: string
): Promise<Blob> {
  const response = await http.post("/admin/reports/batch-retry/export", {
    failedReportIds,
    action,
    banTargetUser,
    note
  }, { responseType: "blob" });
  return response.data as Blob;
}

export async function fetchReportHandleLogs(reportId: number): Promise<ReportHandleLogItem[]> {
  const response = await http.get<ApiResponse<ReportHandleLogItem[]>>(`/admin/reports/${reportId}/logs`);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "获取处理日志失败");
  }
  return response.data.data || [];
}
