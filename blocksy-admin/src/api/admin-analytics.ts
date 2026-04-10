import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface TrendPoint {
  day: string;
  value: number;
}

export interface CommunityActivityPoint {
  communityId: number;
  communityName: string;
  postCount: number;
  commentCount: number;
  reportCount: number;
}

export interface ContentTrendPoint {
  day: string;
  postCount: number;
  commentCount: number;
}

export interface ModerationOverview {
  totalReports: number;
  pendingReports: number;
  handledReports: number;
  pendingRate: number;
  handledRate: number;
}

export interface CommunityRankingItem {
  communityId: number;
  communityName: string;
  postCount: number;
  commentCount: number;
  reportCount: number;
  score: number;
}

export interface AnalyticsMetricDefinition {
  metricCode: string;
  metricName: string;
  definition: string;
  formula: string;
  dataSource: string;
}

export interface AnalyticsConfig {
  defaultWindowDays: number;
  maxWindowDays: number;
  allowedWindowDays: number[];
  metricDefinitions: AnalyticsMetricDefinition[];
  updatedAt?: string;
}

export async function fetchAnalyticsConfig(): Promise<AnalyticsConfig> {
  const { data } = await http.get<ApiResponse<AnalyticsConfig>>("/admin/analytics/config");
  return data.data || {
    defaultWindowDays: 7,
    maxWindowDays: 90,
    allowedWindowDays: [7, 14, 30, 90],
    metricDefinitions: []
  };
}

export async function saveAnalyticsConfig(payload: {
  defaultWindowDays: number;
  maxWindowDays: number;
  allowedWindowDays: number[];
}): Promise<AnalyticsConfig> {
  const { data } = await http.post<ApiResponse<AnalyticsConfig>>("/admin/analytics/config", payload);
  return data.data;
}

export async function fetchGrowthTrend(days: number): Promise<TrendPoint[]> {
  const { data } = await http.get<ApiResponse<TrendPoint[]>>("/admin/analytics/growth", { params: { days } });
  return data.data || [];
}

export async function fetchCommunityActivity(limit = 20, days?: number): Promise<CommunityActivityPoint[]> {
  const { data } = await http.get<ApiResponse<CommunityActivityPoint[]>>("/admin/analytics/community", { params: { limit, days } });
  return data.data || [];
}

export async function fetchContentTrend(days: number): Promise<ContentTrendPoint[]> {
  const { data } = await http.get<ApiResponse<ContentTrendPoint[]>>("/admin/analytics/content", { params: { days } });
  return data.data || [];
}

export async function fetchModerationOverview(): Promise<ModerationOverview> {
  const { data } = await http.get<ApiResponse<ModerationOverview>>("/admin/analytics/moderation");
  return data.data || { totalReports: 0, pendingReports: 0, handledReports: 0, pendingRate: 0, handledRate: 0 };
}

export async function fetchRetentionTrend(days: number): Promise<TrendPoint[]> {
  const { data } = await http.get<ApiResponse<TrendPoint[]>>("/admin/analytics/retention", { params: { days } });
  return data.data || [];
}

export async function fetchCommunityRanking(limit = 20, days?: number): Promise<CommunityRankingItem[]> {
  const { data } = await http.get<ApiResponse<CommunityRankingItem[]>>("/admin/analytics/ranking", { params: { limit, days } });
  return data.data || [];
}
