import { ApiResponse, buildApiUrl } from "../utils/request";
import { getStorage } from "../utils/storage";

export interface UploadResult {
  bucket: string;
  objectKey: string;
  url: string;
  size: number;
  originalFilename: string;
  contentType?: string;
}

export function uploadFile(filePath: string, bizPath = "common"): Promise<UploadResult> {
  return new Promise<UploadResult>((resolve, reject) => {
    const token = getStorage<string>("blocksy_token");
    const header: Record<string, string> = {};
    if (token) {
      header.Authorization = `Bearer ${token}`;
    }
    uni.uploadFile({
      url: buildApiUrl("/files/upload"),
      filePath,
      name: "file",
      formData: { bizPath },
      header,
      success: (res) => {
        try {
          const body = JSON.parse(res.data) as ApiResponse<UploadResult>;
          if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 0) {
            resolve(body.data);
            return;
          }
          reject(new Error(body?.message || "上传失败"));
        } catch (error) {
          reject(error);
        }
      },
      fail: (error) => reject(error)
    });
  });
}
