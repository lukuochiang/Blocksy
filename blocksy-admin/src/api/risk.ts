import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PagedData<T> {
  page: number;
  pageSize: number;
  total: number;
  items: T[];
}

export interface RiskAnomalyItem {
  id: number;
  userId?: number;
  anomalyType: string;
  level: string;
  details?: string;
  processStatus: string;
  handleNote?: string;
  assigneeUserId?: number;
  processedAt?: string;
  createdAt: string;
}

export interface RiskAppealItem {
  id: number;
  userId: number;
  punishLogId?: number;
  appealReason?: string;
  appealContent: string;
  processStatus: string;
  resultNote?: string;
  assigneeUserId?: number;
  processedAt?: string;
  createdAt: string;
}

export async function fetchRiskAnomalies(params: {
  processStatus?: string;
  level?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<RiskAnomalyItem>> {
  const { data } = await http.get<ApiResponse<PagedData<RiskAnomalyItem>>>("/admin/risk/anomalies", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function handleRiskAnomaly(id: number, payload: { processStatus: string; handleNote?: string }): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/risk/anomalies/${id}/handle`, payload);
  if (data.code !== 0) {
    throw new Error(data.message || "处理异常失败");
  }
}

export async function fetchRiskAppeals(params: {
  processStatus?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<RiskAppealItem>> {
  const { data } = await http.get<ApiResponse<PagedData<RiskAppealItem>>>("/admin/risk/appeals", { params });
  return data.data || { page: 1, pageSize: 10, total: 0, items: [] };
}

export async function handleRiskAppeal(id: number, payload: { processStatus: string; resultNote?: string }): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/risk/appeals/${id}/handle`, payload);
  if (data.code !== 0) {
    throw new Error(data.message || "处理申诉失败");
  }
}
