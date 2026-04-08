export interface RoleItem {
  id: number;
  name: string;
  code: string;
  description: string;
  enabled: boolean;
  createdAt: string;
}

const STORAGE_KEY = "blocksy_admin_roles";

function readList(): RoleItem[] {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return [];
  }
  try {
    return JSON.parse(raw) as RoleItem[];
  } catch {
    return [];
  }
}

function writeList(rows: RoleItem[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(rows));
}

export async function fetchRoles(): Promise<RoleItem[]> {
  const rows = readList();
  if (rows.length > 0) {
    return rows;
  }
  const initRows: RoleItem[] = [
    { id: 1, name: "超级管理员", code: "SUPER_ADMIN", description: "全量功能和数据权限", enabled: true, createdAt: new Date().toISOString() },
    { id: 2, name: "审核员", code: "AUDITOR", description: "内容审核、举报处理", enabled: true, createdAt: new Date().toISOString() },
    { id: 3, name: "平台运营", code: "OPERATOR", description: "活动、公告、推送管理", enabled: true, createdAt: new Date().toISOString() }
  ];
  writeList(initRows);
  return initRows;
}

export async function createRole(payload: Pick<RoleItem, "name" | "code" | "description">): Promise<void> {
  const rows = readList();
  const nextId = rows.length ? Math.max(...rows.map((item) => item.id)) + 1 : 1;
  rows.unshift({
    id: nextId,
    name: payload.name,
    code: payload.code,
    description: payload.description,
    enabled: true,
    createdAt: new Date().toISOString()
  });
  writeList(rows);
}

export async function toggleRoleEnabled(id: number, enabled: boolean): Promise<void> {
  const rows = readList().map((item) => (item.id === id ? { ...item, enabled } : item));
  writeList(rows);
}
