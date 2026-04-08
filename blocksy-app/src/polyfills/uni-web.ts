type RequestOptions = {
  url: string;
  method?: string;
  data?: unknown;
  header?: Record<string, string>;
  success?: (res: { statusCode: number; data: unknown }) => void;
  fail?: (error: unknown) => void;
};

function resolveHashUrl(url: string): string {
  return `#${url.startsWith("/") ? url : `/${url}`}`;
}

function request(options: RequestOptions) {
  const method = (options.method || "GET").toUpperCase();
  const headers = options.header || {};
  const hasBody = method !== "GET" && method !== "HEAD";
  fetch(options.url, {
    method,
    headers,
    body: hasBody ? JSON.stringify(options.data ?? {}) : undefined
  })
    .then(async (resp) => {
      const text = await resp.text();
      let data: unknown = text;
      try {
        data = JSON.parse(text);
      } catch {
        // keep raw text
      }
      options.success?.({ statusCode: resp.status, data });
    })
    .catch((error) => options.fail?.(error));
}

export function installUniWebPolyfill() {
  if (typeof window === "undefined") {
    return;
  }
  if ((window as unknown as { uni?: unknown }).uni) {
    return;
  }
  const uniWeb = {
    request,
    uploadFile(options: {
      url: string;
      filePath: string;
      name: string;
      formData?: Record<string, string>;
      header?: Record<string, string>;
      success?: (res: { statusCode: number; data: string }) => void;
      fail?: (error: unknown) => void;
    }) {
      fetch(options.filePath)
        .then((resp) => resp.blob())
        .then((blob) => {
          const form = new FormData();
          form.append(options.name, blob, "upload-file");
          Object.entries(options.formData || {}).forEach(([k, v]) => form.append(k, v));
          return fetch(options.url, {
            method: "POST",
            headers: options.header,
            body: form
          });
        })
        .then(async (resp) => {
          options.success?.({
            statusCode: resp.status,
            data: await resp.text()
          });
        })
        .catch((error) => options.fail?.(error));
    },
    chooseImage(options: { count?: number; success?: (res: { tempFilePaths: string[] }) => void; fail?: (error: unknown) => void }) {
      const input = document.createElement("input");
      input.type = "file";
      input.accept = "image/*";
      input.multiple = (options.count || 1) > 1;
      input.onchange = () => {
        const files = Array.from(input.files || []);
        if (!files.length) {
          options.fail?.(new Error("未选择文件"));
          return;
        }
        const tempFilePaths = files.slice(0, options.count || 1).map((file) => URL.createObjectURL(file));
        options.success?.({ tempFilePaths });
      };
      input.click();
    },
    navigateTo(options: { url: string }) {
      window.location.hash = resolveHashUrl(options.url);
    },
    reLaunch(options: { url: string }) {
      window.location.hash = resolveHashUrl(options.url);
    },
    showToast(options: { title: string }) {
      console.log("[toast]", options.title);
    },
    getStorageSync(key: string) {
      const raw = localStorage.getItem(key);
      if (raw == null) {
        return "";
      }
      try {
        return JSON.parse(raw);
      } catch {
        return raw;
      }
    },
    setStorageSync(key: string, value: unknown) {
      if (typeof value === "string") {
        localStorage.setItem(key, value);
      } else {
        localStorage.setItem(key, JSON.stringify(value));
      }
    },
    removeStorageSync(key: string) {
      localStorage.removeItem(key);
    }
  };
  (window as unknown as { uni: typeof uniWeb }).uni = uniWeb;
}
