import http from "./http";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PlatformSettingItem {
  id: number;
  settingGroup: string;
  settingKey: string;
  settingValue?: string;
  description?: string;
  updatedAt: string;
}

export interface PolicyDocumentItem {
  id: number;
  policyType: string;
  version: string;
  title: string;
  content: string;
  active: boolean;
  createdBy?: number;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export async function fetchPlatformSettings(settingGroup?: string): Promise<PlatformSettingItem[]> {
  const { data } = await http.get<ApiResponse<PlatformSettingItem[]>>("/admin/settings/items", { params: { settingGroup } });
  return data.data || [];
}

export async function savePlatformSetting(payload: {
  settingGroup: string;
  settingKey: string;
  settingValue?: string;
  description?: string;
}): Promise<PlatformSettingItem> {
  const { data } = await http.post<ApiResponse<PlatformSettingItem>>("/admin/settings/items/save", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "保存设置失败");
  }
  return data.data;
}

export async function fetchPolicies(policyType?: string): Promise<PolicyDocumentItem[]> {
  const { data } = await http.get<ApiResponse<PolicyDocumentItem[]>>("/admin/settings/policies", { params: { policyType } });
  return data.data || [];
}

export async function savePolicy(payload: {
  policyType: string;
  version: string;
  title: string;
  content: string;
}): Promise<PolicyDocumentItem> {
  const { data } = await http.post<ApiResponse<PolicyDocumentItem>>("/admin/settings/policies/save", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "保存协议失败");
  }
  return data.data;
}

export async function activatePolicy(id: number): Promise<void> {
  const { data } = await http.post<ApiResponse<unknown>>(`/admin/settings/policies/${id}/activate`);
  if (data.code !== 0) {
    throw new Error(data.message || "激活失败");
  }
}
