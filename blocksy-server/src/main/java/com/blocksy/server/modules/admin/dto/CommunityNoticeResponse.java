package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record CommunityNoticeResponse(
        Long id,
        Long communityId,
        String title,
        String content,
        Integer status,
        Long createdBy,
        Long revokedBy,
        LocalDateTime revokedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
