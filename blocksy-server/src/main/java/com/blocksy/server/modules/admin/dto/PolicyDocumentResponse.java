package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record PolicyDocumentResponse(
        Long id,
        String policyType,
        String version,
        String title,
        String content,
        Boolean active,
        Long createdBy,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
