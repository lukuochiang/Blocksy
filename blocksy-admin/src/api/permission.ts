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

export interface MenuPermissionItem {
  id: number;
  roleCode: string;
  menuKey: string;
  menuName: string;
  menuPath: string;
  enabled: boolean;
  updatedAt: string;
}

export interface PermissionLogItem {
  id: number;
  roleCode: string;
  operatorUserId: number;
  action: string;
  details?: string;
  createdAt: string;
}

export interface DataPermissionItem {
  id: number;
  roleCode: string;
  dataScope: string;
  dataValue: string;
  enabled: boolean;
  updatedAt: string;
}

export interface AdminOperationLogItem {
  id: number;
  module: string;
  action: string;
  operatorUserId: number;
  targetType?: string;
  targetId?: number;
  details?: string;
  createdAt: string;
}

export interface UserBehaviorLogItem {
  id: number;
  userId: number;
  behaviorType: string;
  resourceType?: string;
  resourceId?: number;
  ip?: string;
  device?: string;
  createdAt: string;
}

export async function fetchMenuPermissions(params: {
  roleCode?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<MenuPermissionItem>> {
  const { data } = await http.get<ApiResponse<PagedData<MenuPermissionItem>>>("/admin/permissions/menus", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function assignRoleMenus(payload: { roleCode: string; menuKeys: string[] }): Promise<number> {
  const { data } = await http.post<ApiResponse<number>>("/admin/permissions/menus/assign", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "分配菜单权限失败");
  }
  return data.data || 0;
}

export async function fetchPermissionLogs(params: { roleCode?: string; limit?: number }): Promise<PermissionLogItem[]> {
  const { data } = await http.get<ApiResponse<PermissionLogItem[]>>("/admin/permissions/logs", { params });
  return data.data || [];
}

export async function fetchDataPermissions(params: {
  roleCode?: string;
  dataScope?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<DataPermissionItem>> {
  const { data } = await http.get<ApiResponse<PagedData<DataPermissionItem>>>("/admin/permissions/data", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function assignDataPermissions(payload: {
  roleCode: string;
  dataScope: string;
  dataValues: string[];
}): Promise<number> {
  const { data } = await http.post<ApiResponse<number>>("/admin/permissions/data/assign", payload);
  if (data.code !== 0) {
    throw new Error(data.message || "分配数据权限失败");
  }
  return data.data || 0;
}

export async function fetchAdminOperationLogs(params: {
  module?: string;
  action?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<AdminOperationLogItem>> {
  const { data } = await http.get<ApiResponse<PagedData<AdminOperationLogItem>>>("/admin/permissions/operation-logs", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}

export async function fetchUserBehaviorLogs(params: {
  userId?: number;
  behaviorType?: string;
  page?: number;
  pageSize?: number;
}): Promise<PagedData<UserBehaviorLogItem>> {
  const { data } = await http.get<ApiResponse<PagedData<UserBehaviorLogItem>>>("/admin/permissions/user-behavior-logs", { params });
  return data.data || { page: 1, pageSize: 20, total: 0, items: [] };
}
