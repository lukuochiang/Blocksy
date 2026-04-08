function hasUniStorage(): boolean {
  return typeof uni !== "undefined" && typeof uni.getStorageSync === "function";
}

export function getStorage<T = unknown>(key: string): T | null {
  if (hasUniStorage()) {
    const value = uni.getStorageSync(key);
    return (value ?? null) as T | null;
  }
  const raw = typeof localStorage !== "undefined" ? localStorage.getItem(key) : null;
  if (raw == null) {
    return null;
  }
  try {
    return JSON.parse(raw) as T;
  } catch {
    return raw as T;
  }
}

export function setStorage(key: string, value: unknown): void {
  if (hasUniStorage()) {
    uni.setStorageSync(key, value);
    return;
  }
  if (typeof localStorage === "undefined") {
    return;
  }
  if (typeof value === "string") {
    localStorage.setItem(key, value);
  } else {
    localStorage.setItem(key, JSON.stringify(value));
  }
}

export function removeStorage(key: string): void {
  if (hasUniStorage()) {
    uni.removeStorageSync(key);
    return;
  }
  if (typeof localStorage !== "undefined") {
    localStorage.removeItem(key);
  }
}
