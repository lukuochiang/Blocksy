export interface SensitiveWordItem {
  id: number;
  word: string;
  level: "LOW" | "MEDIUM" | "HIGH";
  enabled: boolean;
  createdAt: string;
}

const STORAGE_KEY = "blocksy_admin_sensitive_words";

function readList(): SensitiveWordItem[] {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return [];
  }
  try {
    return JSON.parse(raw) as SensitiveWordItem[];
  } catch {
    return [];
  }
}

function writeList(rows: SensitiveWordItem[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(rows));
}

export async function fetchSensitiveWords(): Promise<SensitiveWordItem[]> {
  const rows = readList();
  if (rows.length > 0) {
    return rows;
  }
  const initRows: SensitiveWordItem[] = [
    { id: 1, word: "加微信", level: "MEDIUM", enabled: true, createdAt: new Date().toISOString() },
    { id: 2, word: "兼职刷单", level: "HIGH", enabled: true, createdAt: new Date().toISOString() }
  ];
  writeList(initRows);
  return initRows;
}

export async function createSensitiveWord(word: string, level: SensitiveWordItem["level"]): Promise<void> {
  const rows = readList();
  const nextId = rows.length ? Math.max(...rows.map((item) => item.id)) + 1 : 1;
  rows.unshift({
    id: nextId,
    word,
    level,
    enabled: true,
    createdAt: new Date().toISOString()
  });
  writeList(rows);
}

export async function toggleSensitiveWord(id: number, enabled: boolean): Promise<void> {
  const rows = readList().map((item) => (item.id === id ? { ...item, enabled } : item));
  writeList(rows);
}

export async function removeSensitiveWord(id: number): Promise<void> {
  const rows = readList().filter((item) => item.id !== id);
  writeList(rows);
}
