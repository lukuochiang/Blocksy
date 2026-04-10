import http from "./http";

export interface OperationNoteTemplateItem {
  id: number;
  module: string;
  action: string;
  content: string;
  sortNo: number;
  status: number;
}

export interface OperationNoteTemplateLogItem {
  id: number;
  templateId: number;
  operatorUserId: number;
  action: string;
  note?: string;
  createdAt: string;
}

export interface OperationNoteTemplateRuleItem {
  module: string;
  actions: string[];
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function fetchOperationNoteTemplates(params?: {
  module?: string;
  action?: string;
  status?: number;
}): Promise<OperationNoteTemplateItem[]> {
  const { data } = await http.get<ApiResponse<OperationNoteTemplateItem[]>>("/admin/operation-note-templates", { params });
  return data.data ?? [];
}

export async function createOperationNoteTemplate(payload: {
  module: string;
  action: string;
  content: string;
  sortNo?: number;
  status?: number;
}): Promise<OperationNoteTemplateItem> {
  const { data } = await http.post<ApiResponse<OperationNoteTemplateItem>>("/admin/operation-note-templates", payload);
  return data.data;
}

export async function updateOperationNoteTemplate(
  id: number,
  payload: { module: string; action: string; content: string; sortNo?: number; status?: number }
): Promise<OperationNoteTemplateItem> {
  const { data } = await http.put<ApiResponse<OperationNoteTemplateItem>>(`/admin/operation-note-templates/${id}`, payload);
  return data.data;
}

export async function updateOperationNoteTemplateStatus(id: number, status: number): Promise<boolean> {
  const { data } = await http.post<ApiResponse<boolean>>(`/admin/operation-note-templates/${id}/status`, null, {
    params: { status }
  });
  return data.data;
}

export async function fetchOperationNoteTemplateLogs(templateId?: number): Promise<OperationNoteTemplateLogItem[]> {
  const { data } = await http.get<ApiResponse<OperationNoteTemplateLogItem[]>>("/admin/operation-note-templates/logs", {
    params: { templateId }
  });
  return data.data ?? [];
}

export async function fetchOperationNoteTemplateLogsWithFilter(params?: {
  templateId?: number;
  operatorUserId?: number;
  action?: string;
  startAt?: string;
  endAt?: string;
}): Promise<OperationNoteTemplateLogItem[]> {
  const { data } = await http.get<ApiResponse<OperationNoteTemplateLogItem[]>>("/admin/operation-note-templates/logs", {
    params
  });
  return data.data ?? [];
}

export async function getOperationNoteTemplatePermission(): Promise<boolean> {
  const { data } = await http.get<ApiResponse<boolean>>("/admin/operation-note-templates/permission");
  return !!data.data;
}

export async function fetchOperationNoteTemplateRules(): Promise<OperationNoteTemplateRuleItem[]> {
  const { data } = await http.get<ApiResponse<OperationNoteTemplateRuleItem[]>>("/admin/operation-note-templates/rules");
  return data.data ?? [];
}
