package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record MediaAssetResponse(
        Long id,
        Long postId,
        Long communityId,
        Long userId,
        String objectKey,
        String url,
        Long size,
        String contentType,
        Integer status,
        LocalDateTime createdAt
) {
}
