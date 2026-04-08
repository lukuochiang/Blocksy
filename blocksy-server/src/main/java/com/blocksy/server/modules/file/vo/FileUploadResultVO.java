package com.blocksy.server.modules.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public record FileUploadResultVO(
        @Schema(description = "bucket 名称", example = "blocksy-media")
        String bucket,
        @Schema(description = "对象键", example = "common/2026/04/08/abc123.png")
        String objectKey,
        @Schema(description = "文件访问 URL")
        String url,
        @Schema(description = "文件大小（字节）", example = "12345")
        long size,
        @Schema(description = "原始文件名", example = "demo.png")
        String originalFilename,
        @Schema(description = "文件 content-type", example = "image/png")
        String contentType
) {
}
