package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record PushTaskResponse(
        Long id,
        String title,
        String content,
        String targetType,
        String taskStatus,
        LocalDateTime scheduledAt,
        LocalDateTime sentAt,
        Long createdBy,
        LocalDateTime createdAt
) {
}
