package com.blocksy.server.infrastructure.minio.service;

public record MinioUploadResult(
        String bucket,
        String objectKey,
        String url,
        long size,
        String originalFilename,
        String contentType
) {
}
